package com.example.chromaalbum.domain.palette

import android.graphics.Bitmap
import android.net.Uri
import com.example.chromaalbum.domain.model.BlendedPalette

interface PaletteEngine {
    suspend fun extractPalette(bitmap: Bitmap): BlendedPalette
    suspend fun blendPalettes(uris: List<Uri>): BlendedPalette?
}
