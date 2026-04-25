package com.example.chromaalbum.domain.usecase

import android.net.Uri
import com.example.chromaalbum.di.DefaultDispatcher
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import androidx.core.net.toUri

class AutoRegeneratePaletteUseCase(
    private val repository: AlbumRepository,
    private val blendFn: suspend (List<Uri>) -> BlendedPalette?,
    private val dispatcher: CoroutineDispatcher,
    private val debounceMs: Long = 2_000L,
) {
    @Inject
    constructor(
        repository: AlbumRepository,
        blendAlbumPaletteUseCase: BlendAlbumPaletteUseCase,
        @DefaultDispatcher dispatcher: CoroutineDispatcher,
    ) : this(repository, blendAlbumPaletteUseCase::invoke, dispatcher)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    operator fun invoke(albumId: Long): Flow<Unit> =
        repository
            .getPhotosForAlbum(albumId)
            .distinctUntilChanged()
            .debounce(debounceMs)
            .mapLatest { photos ->
                val uris = photos.map { it.uri.toUri() }
                if (uris.isEmpty()) {
                    repository.persistPalette(albumId, BlendedPalette())
                    return@mapLatest
                }
                val palette = blendFn(uris) ?: return@mapLatest
                repository.persistPalette(albumId, palette)
            }.flowOn(dispatcher)
}
