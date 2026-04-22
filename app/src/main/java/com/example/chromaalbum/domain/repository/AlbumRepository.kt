package com.example.chromaalbum.domain.repository

import com.example.chromaalbum.data.local.entity.Album
import com.example.chromaalbum.data.local.entity.Photo
import com.example.chromaalbum.domain.model.BlendedPalette
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun getAllAlbums(): Flow<List<Album>>

    fun getAlbumById(albumId: Long): Flow<Album?>

    suspend fun createAlbum(
        name: String,
        description: String?,
    ): Long

    suspend fun updateAlbum(album: Album)

    suspend fun deleteAlbum(album: Album)

    suspend fun addPhotos(photos: List<Photo>)

    suspend fun removePhoto(photo: Photo)

    fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>>

    suspend fun persistPalette(
        albumId: Long,
        palette: BlendedPalette,
    )
}
