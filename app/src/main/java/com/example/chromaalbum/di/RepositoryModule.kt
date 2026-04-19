package com.example.chromaalbum.di

import android.content.ContentResolver
import android.content.Context
import com.example.chromaalbum.data.helper.UriPersistenceHelper
import com.example.chromaalbum.data.helper.UriPersistenceHelperImpl
import com.example.chromaalbum.data.repository.AlbumRepository
import com.example.chromaalbum.data.repository.AlbumRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAlbumRepository(impl: AlbumRepositoryImpl): AlbumRepository

    @Binds
    @Singleton
    abstract fun bindUriPersistenceHelper(impl: UriPersistenceHelperImpl): UriPersistenceHelper

    companion object {
        @Provides
        @Singleton
        fun provideContentResolver(
            @ApplicationContext context: Context,
        ): ContentResolver = context.contentResolver
    }
}
