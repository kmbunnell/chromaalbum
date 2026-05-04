package com.example.chromaalbum.di

import android.content.Context
import androidx.room.Room
import com.example.chromaalbum.data.local.ChromaAlbumDatabase
import com.example.chromaalbum.data.local.Migration1To2
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
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): ChromaAlbumDatabase =
        Room
            .databaseBuilder(context, ChromaAlbumDatabase::class.java, "chroma_album.db")
            .addMigrations(Migration1To2)
            .build()

    @Provides
    @Singleton
    fun provideAlbumDao(db: ChromaAlbumDatabase): AlbumDao = db.albumDao()

    @Provides
    @Singleton
    fun providePhotoDao(db: ChromaAlbumDatabase): PhotoDao = db.photoDao()
}
