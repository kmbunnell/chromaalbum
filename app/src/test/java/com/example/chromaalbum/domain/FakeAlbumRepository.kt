package com.example.chromaalbum.domain

import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow

class FakeAlbumRepository(private val flow: Flow<List<Album>>) : AlbumRepository {
    override fun getAllAlbums(): Flow<List<Album>> = flow

    override fun getAlbumById(albumId: Long): Flow<Album?> = error("unused")

    override suspend fun createAlbum(name: String, description: String?): Long = error("unused")

    override suspend fun updateAlbum(album: Album): Unit = error("unused")

    override suspend fun deleteAlbum(album: Album): Unit = error("unused")

    override suspend fun addPhotos(photos: List<Photo>): Unit = error("unused")

    override suspend fun removePhoto(photo: Photo): Unit = error("unused")

    override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = error("unused")

    override suspend fun persistPalette(albumId: Long, palette: BlendedPalette): Unit = error("unused")
}

class ThrowingAlbumRepository : AlbumRepository {
    override fun getAllAlbums(): Flow<List<Album>> = throw RuntimeException("db error")

    override fun getAlbumById(albumId: Long): Flow<Album?> = error("unused")

    override suspend fun createAlbum(name: String, description: String?): Long = error("unused")

    override suspend fun updateAlbum(album: Album): Unit = error("unused")

    override suspend fun deleteAlbum(album: Album): Unit = error("unused")

    override suspend fun addPhotos(photos: List<Photo>): Unit = error("unused")

    override suspend fun removePhoto(photo: Photo): Unit = error("unused")

    override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = error("unused")

    override suspend fun persistPalette(albumId: Long, palette: BlendedPalette): Unit = error("unused")
}
