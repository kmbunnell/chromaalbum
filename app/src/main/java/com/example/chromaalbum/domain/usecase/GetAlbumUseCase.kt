package com.example.chromaalbum.domain.usecase

import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAlbumUseCase
    @Inject
    constructor(
        private val repository: AlbumRepository,
    ) {
        operator fun invoke(albumId: Long): Flow<Result<Album, String>> =
            flow { emitAll(repository.getAlbumById(albumId)) }
                .map<Album?, Result<Album, String>> { album ->
                    if (album != null) {
                        Result.Success(album)
                    } else {
                        Result.Failure("Album $albumId not found")
                    }
                }.catch { e ->
                    if (e is CancellationException) throw e
                    emit(Result.Failure(e.message ?: "Unknown error"))
                }
    }
