package com.example.chromaalbum.data.palette

import android.graphics.Bitmap
import androidx.core.graphics.scale
import androidx.palette.graphics.Palette
import com.example.chromaalbum.di.DefaultDispatcher
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.SwatchData
import com.example.chromaalbum.domain.palette.PaletteEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PaletteEngineImpl
    @Inject
    constructor(
        @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    ) : PaletteEngine {
        override suspend fun extractPalette(bitmap: Bitmap): BlendedPalette =
            withContext(dispatcher) {
                val scaled = bitmap.scale(100, 100)
                val palette = Palette.from(scaled).generate()
                if (scaled !== bitmap) scaled.recycle()
                BlendedPalette(
                    vibrant = palette.vibrantSwatch?.toSwatchData(),
                    darkVibrant = palette.darkVibrantSwatch?.toSwatchData(),
                    lightVibrant = palette.lightVibrantSwatch?.toSwatchData(),
                    muted = palette.mutedSwatch?.toSwatchData(),
                    darkMuted = palette.darkMutedSwatch?.toSwatchData(),
                    lightMuted = palette.lightMutedSwatch?.toSwatchData(),
                )
            }
    }

private fun Palette.Swatch.toSwatchData() =
    SwatchData(
        hex = "#%06X".format(rgb and 0xFFFFFF),
        titleText = "#%06X".format(titleTextColor and 0xFFFFFF),
    )
