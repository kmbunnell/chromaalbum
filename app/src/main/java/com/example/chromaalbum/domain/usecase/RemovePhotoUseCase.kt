package com.example.chromaalbum.domain.usecase

import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.repository.AlbumRepository
import javax.inject.Inject

class RemovePhotoUseCase
    @Inject
    constructor(
        private val repository: AlbumRepository,
    ) {
        suspend operator fun invoke(photo: Photo) {
            repository.removePhoto(photo)
        }
    }
