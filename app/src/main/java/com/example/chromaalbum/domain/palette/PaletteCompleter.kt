package com.example.chromaalbum.domain.palette

import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.SwatchData
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

class PaletteCompleter
    @Inject
    constructor() {
        fun complete(palette: BlendedPalette): BlendedPalette {
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

        private fun firstNonNull (palette: BlendedPalette): SwatchData =
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

internal fun hexToHsl(hex: String): Triple<Float, Float, Float> {
    val c = hex.removePrefix("#").toLong(16).toInt()
    val r = ((c shr 16) and 0xFF) / 255f
    val g = ((c shr 8) and 0xFF) / 255f
    val b = (c and 0xFF) / 255f
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    val l = (max + min) / 2f
    val s = if (delta == 0f) 0f else delta / (1f - abs(2f * l - 1f))
    val h =
        when {
            delta == 0f -> 0f
            max == r -> 60f * ((g - b) / delta + if (g < b) 6f else 0f)
            max == g -> 60f * ((b - r) / delta + 2f)
            else -> 60f * ((r - g) / delta + 4f)
        }
    return Triple(h, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))
}

internal fun hslToHex(
    h: Float,
    s: Float,
    l: Float,
): String {
    val c = (1f - abs(2f * l - 1f)) * s
    val x = c * (1f - abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f
    val (r1, g1, b1) =
        when ((h / 60f).toInt().coerceIn(0, 5)) {
            0 -> Triple(c, x, 0f)
            1 -> Triple(x, c, 0f)
            2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c)
            4 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
    val r = ((r1 + m) * 255f).roundToInt().coerceIn(0, 255)
    val g = ((g1 + m) * 255f).roundToInt().coerceIn(0, 255)
    val b = ((b1 + m) * 255f).roundToInt().coerceIn(0, 255)
    return "#%02X%02X%02X".format(r, g, b)
}
