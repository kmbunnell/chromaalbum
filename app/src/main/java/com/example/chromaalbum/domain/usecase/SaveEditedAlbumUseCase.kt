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

class SaveEditedAlbumUseCase(
    private val updateAlbumUseCase: UpdateAlbumUseCase,
    private val addPhotosUseCase: AddPhotosUseCase,
    private val blendFn: suspend (List<Uri>) -> BlendedPalette?,
    private val repository: AlbumRepository,
) {
    @Inject
    constructor(
        updateAlbumUseCase: UpdateAlbumUseCase,
        addPhotosUseCase: AddPhotosUseCase,
        blendAlbumPaletteUseCase: BlendAlbumPaletteUseCase,
        repository: AlbumRepository,
    ) : this(updateAlbumUseCase, addPhotosUseCase, blendAlbumPaletteUseCase::invoke, repository)

    operator fun invoke(
        albumId: Long,
        name: String,
        description: String?,
        newUris: List<String>,
        allUris: List<String>,
    ): Flow<Result<Unit, String>> =
        flow {
            val updateResult = updateAlbumUseCase(albumId, name, description).first()
            if (updateResult is Result.Failure) {
                emit(Result.Failure(updateResult.error))
                return@flow
            }
            if (newUris.isNotEmpty()) {
                addPhotosUseCase(albumId, newUris)
                val palette = blendFn(allUris.map { it.toUri() })
                if (palette != null) {
                    repository.persistPalette(albumId, palette)
                }
            }
            emit(Result.Success(Unit))
        }.catch { e ->
            if (e is CancellationException) throw e
            emit(Result.Failure(e.message ?: "Unknown error"))
        }
}
