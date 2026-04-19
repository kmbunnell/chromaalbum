package com.example.chromaalbum.domain.palette

import android.graphics.Bitmap
import com.example.chromaalbum.domain.model.BlendedPalette

interface PaletteEngine {
    suspend fun extractPalette(bitmap: Bitmap): BlendedPalette
}
