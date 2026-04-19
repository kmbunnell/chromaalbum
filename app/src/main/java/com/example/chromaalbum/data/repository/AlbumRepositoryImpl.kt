package com.example.chromaalbum.data.repository

import androidx.room.withTransaction
import com.example.chromaalbum.data.local.ChromaAlbumDatabase
import com.example.chromaalbum.data.local.dao.AlbumDao
import com.example.chromaalbum.data.local.dao.PhotoDao
import com.example.chromaalbum.data.local.entity.Album
import com.example.chromaalbum.data.local.entity.Photo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AlbumRepositoryImpl
    @Inject
    constructor(
        private val albumDao: AlbumDao,
        private val photoDao: PhotoDao,
        private val db: ChromaAlbumDatabase,
    ) : AlbumRepository {
        override fun getAllAlbums(): Flow<List<Album>> = albumDao.getAll()

        override fun getAlbumById(albumId: Long): Flow<Album?> = albumDao.getById(albumId)

        override suspend fun createAlbum(
            name: String,
            description: String?,
        ): Long {
            val now = System.currentTimeMillis()
            return albumDao.insert(
                Album(
                    name = name,
                    description = description,
                    createdAt = now,
                    updatedAt = now,
                    coverPhotoUri = null,
                    paletteJson = Album.EMPTY_PALETTE_JSON,
                ),
            )
        }

        override suspend fun updateAlbum(album: Album) = albumDao.update(album)

        override suspend fun deleteAlbum(album: Album) = albumDao.delete(album)

        override suspend fun addPhotos(
            albumId: Long,
            uris: List<String>,
        ) = db.withTransaction {
            if (uris.isEmpty()) return@withTransaction
            val base = photoDao.getCountOnce(albumId)
            val now = System.currentTimeMillis()
            photoDao.insertAll(
                uris.mapIndexed { i, uri ->
                    Photo(albumId = albumId, uri = uri, addedAt = now, sortOrder = base + i)
                },
            )
            val album = albumDao.getByIdOnce(albumId) ?: return@withTransaction
            if (album.coverPhotoUri == null) {
                albumDao.update(album.copy(coverPhotoUri = uris.first()))
            }
        }

        override suspend fun removePhoto(photo: Photo) =
            db.withTransaction {
                photoDao.delete(photo)
                val album = albumDao.getByIdOnce(photo.albumId) ?: return@withTransaction
                if (album.coverPhotoUri == photo.uri) {
                    albumDao.update(album.copy(coverPhotoUri = photoDao.getFirstPhotoOnce(photo.albumId)?.uri))
                }
            }

        override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = photoDao.getByAlbumId(albumId)

        override suspend fun updateAlbumPalette(
            albumId: Long,
            dominantColor: String,
            paletteJson: String,
        ) = albumDao.updatePalette(albumId, dominantColor, paletteJson, System.currentTimeMillis())
    }
