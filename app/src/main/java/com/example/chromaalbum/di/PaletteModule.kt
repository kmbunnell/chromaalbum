package com.example.chromaalbum.di

import android.content.Context
import coil3.ImageLoader
import com.example.chromaalbum.data.palette.PaletteEngineImpl
import com.example.chromaalbum.data.palette.PhotoLoaderImpl
import com.example.chromaalbum.domain.palette.PaletteEngine
import com.example.chromaalbum.domain.palette.PhotoLoader
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PaletteModule {
    @Binds
    @Singleton
    abstract fun bindPaletteEngine(impl: PaletteEngineImpl): PaletteEngine

    @Binds
    @Singleton
    abstract fun bindPhotoLoader(impl: PhotoLoaderImpl): PhotoLoader

    companion object {
        @Provides
        @Singleton
        fun provideImageLoader(
            @ApplicationContext context: Context,
        ): ImageLoader = ImageLoader(context)
    }
}
