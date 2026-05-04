package com.example.chromaalbum.domain.usecase

import android.net.Uri
import androidx.core.net.toUri
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SaveNewAlbumUseCase(
    private val createAlbumUseCase: CreateAlbumUseCase,
    private val addPhotosUseCase: AddPhotosUseCase,
    private val blendFn: suspend (List<Uri>) -> BlendedPalette?,
    private val repository: AlbumRepository,
) {
    @Inject
    constructor(
        createAlbumUseCase: CreateAlbumUseCase,
        addPhotosUseCase: AddPhotosUseCase,
        blendAlbumPaletteUseCase: BlendAlbumPaletteUseCase,
        repository: AlbumRepository,
    ) : this(createAlbumUseCase, addPhotosUseCase, blendAlbumPaletteUseCase::invoke, repository)

    operator fun invoke(
        name: String,
        description: String?,
        uris: List<String>,
    ): Flow<Result<Long, String>> =
        flow {
            val createResult = createAlbumUseCase(name, description).first()
            if (createResult is Result.Failure) {
                emit(Result.Failure(createResult.error))
                return@flow
            }
            val albumId = (createResult as Result.Success).data
            if (uris.isNotEmpty()) {
                addPhotosUseCase(albumId, uris)
                val palette = blendFn(uris.map { it.toUri() })
                if (palette != null) {
                    repository.persistPalette(albumId, palette)
                }
            }
            emit(Result.Success(albumId))
        }.catch { e ->
            if (e is CancellationException) throw e
            emit(Result.Failure(e.message ?: "Unknown error"))
        }
}
