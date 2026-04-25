package com.example.chromaalbum.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.chromaalbum.data.local.dao.AlbumDao
import com.example.chromaalbum.data.local.dao.PhotoDao
import com.example.chromaalbum.data.local.entity.AlbumEntity
import com.example.chromaalbum.data.local.entity.PhotoEntity

@Database(entities = [AlbumEntity::class, PhotoEntity::class], version = 1, exportSchema = true)
abstract class ChromaAlbumDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao

    abstract fun photoDao(): PhotoDao
}
