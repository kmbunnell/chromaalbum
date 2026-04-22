package com.example.chromaalbum.domain.usecase

import com.example.chromaalbum.data.local.entity.Photo
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RemovePhotoUseCase
    @Inject
    constructor(
        private val repository: AlbumRepository,
    ) {
        suspend operator fun invoke(photo: Photo) {
            val album = repository.getAlbumById(photo.albumId).first() ?: return
            repository.removePhoto(photo)
            if (album.coverPhotoUri == photo.uri) {
                val newCover = repository.getPhotosForAlbum(photo.albumId).first().firstOrNull()?.uri
                repository.updateAlbum(album.copy(coverPhotoUri = newCover))
            }
        }
    }
