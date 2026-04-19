package com.example.chromaalbum.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.chromaalbum.data.local.entity.Photo
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE albumId = :albumId ORDER BY sortOrder ASC")
    fun getByAlbumId(albumId: Long): Flow<List<Photo>>

    @Query("SELECT COUNT(*) FROM photos WHERE albumId = :albumId")
    fun getPhotoCount(albumId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM photos WHERE albumId = :albumId")
    suspend fun getCountOnce(albumId: Long): Int

    @Query("SELECT * FROM photos WHERE albumId = :albumId ORDER BY sortOrder ASC LIMIT 1")
    suspend fun getFirstPhotoOnce(albumId: Long): Photo?

    @Insert
    suspend fun insert(photo: Photo): Long

    @Insert
    suspend fun insertAll(photos: List<Photo>)

    @Delete
    suspend fun delete(photo: Photo)

    @Query("UPDATE photos SET sortOrder = :sortOrder WHERE id = :photoId")
    suspend fun updateSortOrder(
        photoId: Long,
        sortOrder: Int,
    )
}
