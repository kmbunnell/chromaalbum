package com.example.chromaalbum.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.chromaalbum.domain.model.BlendedPalette

internal fun dynamicColorScheme(
    paletteJson: String?,
    isDark: Boolean,
): ColorScheme {
    val palette = paletteJson?.let { BlendedPalette.fromJson(it) }
    return if (isDark) mapToDarkColorScheme(palette) else mapToLightColorScheme(palette)
}

@Composable
fun rememberDynamicColorScheme(
    paletteJson: String?,
    isDark: Boolean,
): ColorScheme = remember(paletteJson, isDark) { dynamicColorScheme(paletteJson, isDark) }
