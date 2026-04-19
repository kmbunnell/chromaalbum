package com.example.chromaalbum.di

import com.example.chromaalbum.data.palette.PaletteEngineImpl
import com.example.chromaalbum.domain.palette.PaletteEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PaletteModule {
    @Binds
    @Singleton
    abstract fun bindPaletteEngine(impl: PaletteEngineImpl): PaletteEngine
}
