package com.example.chromaalbum.data.repository

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.withTransaction
import com.example.chromaalbum.data.helper.UriPersistenceHelper
import com.example.chromaalbum.data.local.ChromaAlbumDatabase
import com.example.chromaalbum.data.local.dao.AlbumDao
import com.example.chromaalbum.data.local.dao.PhotoDao
import com.example.chromaalbum.data.local.entity.Album
import com.example.chromaalbum.data.local.entity.Photo
import com.example.chromaalbum.di.IoDispatcher
import com.example.chromaalbum.domain.model.BlendedPalette
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AlbumRepositoryImpl
    @Inject
    constructor(
        private val albumDao: AlbumDao,
        private val photoDao: PhotoDao,
        private val db: ChromaAlbumDatabase,
        private val uriPersistenceHelper: UriPersistenceHelper,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
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
    ) {
        if (uris.isEmpty()) return
        withContext(ioDispatcher) {
            val persisted = mutableListOf<Uri>()
            try {
                uris.forEach { str ->
                    val uri = str.toUri()
                    uriPersistenceHelper.persist(uri)
                    persisted += uri
                }
                db.withTransaction {
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
            } catch (e: Exception) {
                releaseAll(persisted)
                throw e
            }
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

    override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> =
        photoDao.getByAlbumId(albumId)

    override suspend fun persistPalette(
        albumId: Long,
        palette: BlendedPalette,
    ) {
        albumDao.updatePalette(
            albumId,
            palette.dominantHex(),
            Json.encodeToString(palette),
            System.currentTimeMillis()
        )
    }

    private fun releaseAll(uris: List<Uri>) {
        uris.forEach {
            try {
                uriPersistenceHelper.release(it)
            } catch (_: SecurityException) {
            }
        }
    }
}
