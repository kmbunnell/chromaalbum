package com.example.chromaalbum.domain.usecase

import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UpdateAlbumUseCase
    @Inject
    constructor(
        private val repository: AlbumRepository,
    ) {
        operator fun invoke(
            id: Long,
            name: String,
            description: String?,
        ): Flow<Result<Unit, String>> =
            flow<Result<Unit, String>> {
                val album =
                    repository.getAlbumById(id).first()
                        ?: throw IllegalArgumentException("Album $id not found")
                repository.updateAlbum(
                    album.copy(
                        name = name.trim(),
                        description = description?.trim()?.takeIf { it.isNotEmpty() },
                    ),
                )
                emit(Result.Success(Unit))
            }.catch { e ->
                if (e is CancellationException) throw e
                emit(Result.Failure(e.message ?: "Unknown error"))
            }
    }
