package com.example.chromaalbum.domain.usecase

import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DeleteAlbumUseCase
    @Inject
    constructor(
        private val repository: AlbumRepository,
    ) {
        operator fun invoke(album: Album): Flow<Result<Unit, String>> =
            flow<Result<Unit, String>> {
                repository.deleteAlbum(album)
                emit(Result.Success(Unit))
            }.catch { e ->
                if (e is CancellationException) throw e
                emit(Result.Failure(e.message ?: "Unknown error"))
            }
    }
