package com.example.chromaalbum.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.chromaalbum.data.local.entity.Album
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Query("SELECT * FROM albums ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<Album>>

    @Query("SELECT * FROM albums WHERE id = :albumId")
    fun getById(albumId: Long): Flow<Album?>

    @Insert
    suspend fun insert(album: Album): Long

    @Update
    suspend fun update(album: Album)

    @Delete
    suspend fun delete(album: Album)

    @Query(
        "UPDATE albums SET dominantColor = :dominantColor, paletteJson = :paletteJson, updatedAt = :updatedAt WHERE id = :albumId"
    )
    suspend fun updatePalette(albumId: Long, dominantColor: String, paletteJson: String, updatedAt: Long)
}
