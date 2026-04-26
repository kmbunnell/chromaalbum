package com.example.chromaalbum.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.palette.adjustForContrast
import com.example.chromaalbum.domain.palette.compositeColorOverBackground
import com.example.chromaalbum.domain.palette.hexToRgb

private const val SURFACE = "#FFFFFF"
private const val BACKGROUND = "#FAFAFA"

private fun String.toColor(): Color {
    val rgb = hexToRgb(this)
    return Color(
        red = (rgb shr 16 and 0xFF) / 255f,
        green = (rgb shr 8 and 0xFF) / 255f,
        blue = (rgb and 0xFF) / 255f,
    )
}

fun mapToLightColorScheme(palette: BlendedPalette?): ColorScheme {
    if (palette == null || palette.isEmpty()) return DefaultColorSchemes.lightDefault

    val vibrant = palette.vibrant ?: return DefaultColorSchemes.lightDefault
    val muted = palette.muted ?: return DefaultColorSchemes.lightDefault
    val lightVibrant = palette.lightVibrant ?: return DefaultColorSchemes.lightDefault
    val darkVibrant = palette.darkVibrant ?: return DefaultColorSchemes.lightDefault

    val primary = vibrant.hex.toColor()
    val secondary = darkVibrant.hex.toColor()

    val onPrimary = adjustForContrast(vibrant.titleText, vibrant.hex).toColor()

    val onSurfaceHex =
        adjustForContrast(
            adjustForContrast(muted.titleText, SURFACE),
            BACKGROUND,
        )
    val onSurface = onSurfaceHex.toColor()

    val primaryContainer = compositeColorOverBackground(lightVibrant.hex, SURFACE, 0.20).toColor()
    val outline = compositeColorOverBackground(muted.hex, SURFACE, 0.50).toColor()

    return lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        primaryContainer = primaryContainer,
        background = BACKGROUND.toColor(),
        surface = SURFACE.toColor(),
        onSurface = onSurface,
        outline = outline,
    )
}
