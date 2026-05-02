package com.example.chromaalbum.domain.usecase

import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPhotosForAlbumUseCase
    @Inject
    constructor(
        private val repository: AlbumRepository,
    ) {
        operator fun invoke(albumId: Long): Flow<Result<List<Photo>, String>> =
            flow { emitAll(repository.getPhotosForAlbum(albumId)) }
                .map<List<Photo>, Result<List<Photo>, String>> { Result.Success(it) }
                .catch { e ->
                    if (e is CancellationException) throw e
                    emit(Result.Failure(e.message ?: "Unknown error"))
                }
    }
