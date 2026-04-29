package com.example.chromaalbum.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.lerp
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

private fun lerp(
    start: ColorScheme,
    stop: ColorScheme,
    fraction: Float,
): ColorScheme =
    start.copy(
        primary = lerp(start.primary, stop.primary, fraction),
        onPrimary = lerp(start.onPrimary, stop.onPrimary, fraction),
        primaryContainer = lerp(start.primaryContainer, stop.primaryContainer, fraction),
        secondary = lerp(start.secondary, stop.secondary, fraction),
        background = lerp(start.background, stop.background, fraction),
        surface = lerp(start.surface, stop.surface, fraction),
        onSurface = lerp(start.onSurface, stop.onSurface, fraction),
        outline = lerp(start.outline, stop.outline, fraction),
    )

@Composable
internal fun animateColorScheme(target: ColorScheme): ColorScheme {
    var from by remember { mutableStateOf(target) }
    var to by remember { mutableStateOf(target) }
    val fraction = remember { Animatable(1f) }

    LaunchedEffect(target) {
        from = lerp(from, to, fraction.value)
        to = target
        fraction.snapTo(0f)
        fraction.animateTo(1f, animationSpec = tween(400))
    }

    return lerp(from, to, fraction.value)
}

@Composable
internal fun animateDynamicColorScheme(
    paletteJson: String?,
    isDark: Boolean,
): ColorScheme {
    val target = rememberDynamicColorScheme(paletteJson, isDark)
    return animateColorScheme(target)
}
