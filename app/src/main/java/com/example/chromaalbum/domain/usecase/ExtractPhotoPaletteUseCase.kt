package com.example.chromaalbum.domain.usecase

import android.net.Uri
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.palette.PaletteEngine
import com.example.chromaalbum.domain.palette.PhotoLoader
import javax.inject.Inject

class ExtractPhotoPaletteUseCase
    @Inject
    constructor(
        private val photoLoader: PhotoLoader,
        private val paletteEngine: PaletteEngine,
    ) {
        suspend operator fun invoke(uri: Uri): BlendedPalette {
            val bitmap = photoLoader.load(uri)
            return paletteEngine.extractPalette(bitmap)
        }
    }
