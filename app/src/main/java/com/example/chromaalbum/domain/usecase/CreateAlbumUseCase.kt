package com.example.chromaalbum.domain.usecase

import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CreateAlbumUseCase
    @Inject
    constructor(
        private val repository: AlbumRepository,
    ) {
        operator fun invoke(
            name: String,
            description: String?,
        ): Flow<Result<Long, String>> =
            flow<Result<Long, String>> {
                val id =
                    repository.createAlbum(
                        name.trim(),
                        description?.trim()?.takeIf { it.isNotEmpty() },
                    )
                emit(Result.Success(id))
            }.catch { e ->
                if (e is CancellationException) throw e
                emit(Result.Failure(e.message ?: "Unknown error"))
            }
    }
