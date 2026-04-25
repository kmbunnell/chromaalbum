package com.example.chromaalbum.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.chromaalbum.data.local.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :albumId")
    fun getById(albumId: Long): Flow<AlbumEntity?>

    @Query("SELECT * FROM albums WHERE id = :albumId")
    suspend fun getByIdOnce(albumId: Long): AlbumEntity?

    @Insert
    suspend fun insert(album: AlbumEntity): Long

    @Update
    suspend fun update(album: AlbumEntity)

    @Delete
    suspend fun delete(album: AlbumEntity)

    @Query("UPDATE albums SET paletteJson = :paletteJson, updatedAt = :updatedAt WHERE id = :albumId")
    suspend fun updatePaletteJson(
        albumId: Long,
        paletteJson: String,
        updatedAt: Long,
    )
}
