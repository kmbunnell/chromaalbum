package com.example.chromaalbum.di

import android.content.Context
import androidx.room.Room
import com.example.chromaalbum.data.local.ChromaAlbumDatabase
import com.example.chromaalbum.data.local.dao.AlbumDao
import com.example.chromaalbum.data.local.dao.PhotoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ChromaAlbumDatabase =
        // Add .addMigrations(...) here before incrementing the schema version
        Room.databaseBuilder(context, ChromaAlbumDatabase::class.java, "chroma_album.db").build()

    @Provides
    @Singleton
    fun provideAlbumDao(db: ChromaAlbumDatabase): AlbumDao = db.albumDao()

    @Provides
    @Singleton
    fun providePhotoDao(db: ChromaAlbumDatabase): PhotoDao = db.photoDao()
}
