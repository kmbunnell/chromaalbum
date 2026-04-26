package com.example.chromaalbum.ui.theme

import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.SwatchData
import org.junit.Assert.assertEquals
import org.junit.Test

class DynamicColorSchemeTest {

    private val validPalette =
        BlendedPalette(
            vibrant = SwatchData("#6200EE", "#FFFFFF"),
            darkVibrant = SwatchData("#3700B3", "#FFFFFF"),
            lightVibrant = SwatchData("#BB86FC", "#000000"),
            muted = SwatchData("#607D8B", "#FFFFFF"),
            darkMuted = SwatchData("#37474F", "#FFFFFF"),
            lightMuted = SwatchData("#90A4AE", "#000000"),
        )
    private val validPaletteJson = validPalette.toJson()

    @Test
    fun dynamicColorScheme_givenNullJson_whenLightMode_thenReturnsLightFallback() {
        val result = dynamicColorScheme(paletteJson = null, isDark = false)

        assertEquals(mapToLightColorScheme(null), result)
    }

    @Test
    fun dynamicColorScheme_givenNullJson_whenDarkMode_thenReturnsDarkFallback() {
        val result = dynamicColorScheme(paletteJson = null, isDark = true)

        assertEquals(mapToDarkColorScheme(null), result)
    }

    @Test
    fun dynamicColorScheme_givenInvalidJson_whenLightMode_thenReturnsLightFallback() {
        val result = dynamicColorScheme(paletteJson = "not-valid-json", isDark = false)

        assertEquals(mapToLightColorScheme(null), result)
    }

    @Test
    fun dynamicColorScheme_givenValidJson_whenLightMode_thenReturnsMappedLightScheme() {
        val expected = mapToLightColorScheme(validPalette)
        val result = dynamicColorScheme(paletteJson = validPaletteJson, isDark = false)

        assertEquals(expected.primary, result.primary)
        assertEquals(expected.onPrimary, result.onPrimary)
        assertEquals(expected.secondary, result.secondary)
        assertEquals(expected.primaryContainer, result.primaryContainer)
        assertEquals(expected.background, result.background)
        assertEquals(expected.surface, result.surface)
        assertEquals(expected.onSurface, result.onSurface)
        assertEquals(expected.outline, result.outline)
    }

    @Test
    fun dynamicColorScheme_givenValidJson_whenDarkMode_thenReturnsMappedDarkScheme() {
        val expected = mapToDarkColorScheme(validPalette)
        val result = dynamicColorScheme(paletteJson = validPaletteJson, isDark = true)

        assertEquals(expected.primary, result.primary)
        assertEquals(expected.onPrimary, result.onPrimary)
        assertEquals(expected.primaryContainer, result.primaryContainer)
        assertEquals(expected.secondary, result.secondary)
        assertEquals(expected.surface, result.surface)
        assertEquals(expected.background, result.background)
        assertEquals(expected.onSurface, result.onSurface)
        assertEquals(expected.outline, result.outline)
    }
}
