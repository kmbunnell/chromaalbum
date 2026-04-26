package com.example.chromaalbum.domain.palette

import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.SwatchData
import javax.inject.Inject

interface PaletteCompleter {
    fun complete(palette: BlendedPalette): BlendedPalette
}

class PaletteCompleterImpl
    @Inject
    constructor() : PaletteCompleter {
        override fun complete(palette: BlendedPalette): BlendedPalette {
            if (palette.isEmpty()) return palette
            val vibrant = palette.vibrant ?: firstNonNull(palette)
            val muted = palette.muted ?: derive(vibrant, saturationScale = 0.4f)
            return BlendedPalette(
                vibrant = vibrant,
                darkVibrant = palette.darkVibrant ?: derive(vibrant, lightnessScale = 0.6f),
                lightVibrant = palette.lightVibrant ?: derive(vibrant, lightnessScale = 1.4f),
                muted = muted,
                darkMuted = palette.darkMuted ?: derive(muted, lightnessScale = 0.6f),
                lightMuted = palette.lightMuted ?: derive(muted, lightnessScale = 1.4f),
            )
        }

        private fun firstNonNull(palette: BlendedPalette): SwatchData =
            checkNotNull(
                palette.muted ?: palette.darkMuted
                    ?: palette.darkVibrant ?: palette.lightVibrant ?: palette.lightMuted,
            )

        private fun derive(
            source: SwatchData,
            lightnessScale: Float = 1f,
            saturationScale: Float = 1f,
        ): SwatchData {
            val (h, s, l) = hexToHsl(source.hex)
            val newS = (s * saturationScale).coerceIn(0f, 1f)
            val newL = (l * lightnessScale).coerceIn(0f, 0.95f)
            val titleText = if (newL < 0.5f) "#FFFFFF" else "#000000"
            return SwatchData(hex = hslToHex(h, newS, newL), titleText = titleText)
        }
    }
