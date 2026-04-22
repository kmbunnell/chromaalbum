package com.example.chromaalbum.domain.usecase

import com.example.chromaalbum.data.local.entity.Photo
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddPhotosUseCase
    @Inject
    constructor(
        private val repository: AlbumRepository,
    ) {
        suspend operator fun invoke(
            albumId: Long,
            uris: List<String>,
        ) {
            if (uris.isEmpty()) return
            val base = repository.getPhotosForAlbum(albumId).first().size
            val album = repository.getAlbumById(albumId).first() ?: return
            val now = System.currentTimeMillis()
            repository.addPhotos(
                uris.mapIndexed { i, uri ->
                    Photo(albumId = albumId, uri = uri, addedAt = now, sortOrder = base + i)
                },
            )
            if (album.coverPhotoUri == null) {
                repository.updateAlbum(album.copy(coverPhotoUri = uris.first()))
            }
        }
    }
