package com.example.chromaalbum.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.chromaalbum.domain.model.BlendedPalette
import kotlin.math.roundToInt
import com.example.chromaalbum.domain.model.SwatchData
import com.example.chromaalbum.domain.palette.compositeColorOverBackground
import com.example.chromaalbum.domain.palette.contrastRatio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val SURFACE = "#FFFFFF"
private const val BACKGROUND = "#FAFAFA"

/** Converts a `#RRGGBB` hex string to a Compose [Color] for assertion comparisons. */
private fun String.toColor(): Color {
    val rgb = removePrefix("#").toInt(16)
    return Color(
        red = (rgb shr 16 and 0xFF) / 255f,
        green = (rgb shr 8 and 0xFF) / 255f,
        blue = (rgb and 0xFF) / 255f,
    )
}

/** Converts a Compose [Color] back to a `#RRGGBB` hex string. */
private fun Color.toHex(): String =
    "#%02X%02X%02X".format(
        (red * 255).roundToInt(),
        (green * 255).roundToInt(),
        (blue * 255).roundToInt(),
    )

class ColorSchemeMapperTest {

    @Test
    fun mapToLightColorScheme_givenNullPalette_whenCalled_thenReturnsLightDefault() {
        val result = mapToLightColorScheme(null)

        assertEquals(DefaultColorSchemes.lightDefault, result)
    }

    @Test
    fun mapToLightColorScheme_givenAllNullSwatches_whenCalled_thenReturnsLightDefault() {
        val result = mapToLightColorScheme(BlendedPalette())

        assertEquals(DefaultColorSchemes.lightDefault, result)
    }

    @Test
    fun mapToLightColorScheme_givenPassingContrastOnPrimary_whenCalled_thenOnPrimaryIsUnchanged() {
        // White text on a dark vibrant color passes 4.5:1 easily
        val palette = buildValidPalette(vibrantHex = "#1A237E", vibrantTitle = "#FFFFFF")

        val result = mapToLightColorScheme(palette)

        assertEquals("#FFFFFF".toColor(), result.onPrimary)
    }

    @Test
    fun mapToLightColorScheme_givenLowContrastOnPrimary_whenCalled_thenOnPrimaryIsAdjustedToPassAA() {
        // Medium-gray text on a similar-luminance vibrant fails 4.5:1
        val palette = buildValidPalette(vibrantHex = "#888888", vibrantTitle = "#AAAAAA")

        val result = mapToLightColorScheme(palette)

        assertTrue(contrastRatio(result.onPrimary.toHex(), "#888888") >= 4.5)
    }

    @Test
    fun mapToLightColorScheme_givenLowContrastOnSurface_whenCalled_thenOnSurfacePassesAAVsBothSurfaceAndBackground() {
        // Near-white muted text against white surface — both constraints must be satisfied
        val palette = buildValidPalette(mutedHex = "#E0E0E0", mutedTitle = "#D0D0D0")

        val result = mapToLightColorScheme(palette)

        val onSurfaceHex = result.onSurface.toHex()
        assertTrue(contrastRatio(onSurfaceHex, SURFACE) >= 4.5)
        assertTrue(contrastRatio(onSurfaceHex, BACKGROUND) >= 4.5)
    }

    @Test
    fun mapToLightColorScheme_givenValidPalette_whenCalled_thenOutlineIsCompositedMutedAt50OverWhite() {
        val palette = buildValidPalette(mutedHex = "#607D8B")

        val result = mapToLightColorScheme(palette)

        val expected = compositeColorOverBackground("#607D8B", SURFACE, 0.5).toColor()
        assertEquals(expected, result.outline)
    }

    @Test
    fun mapToLightColorScheme_givenValidPalette_whenCalled_thenPrimaryContainerIsCompositedLightVibrantAt20OverWhite() {
        val palette = buildValidPalette(lightVibrantHex = "#B39DDB")

        val result = mapToLightColorScheme(palette)

        val expected = compositeColorOverBackground("#B39DDB", SURFACE, 0.2).toColor()
        assertEquals(expected, result.primaryContainer)
    }

    @Test
    fun mapToDarkColorScheme_givenNullPalette_whenCalled_thenReturnsDarkDefault() {
        val result = mapToDarkColorScheme(null)

        assertEquals(DefaultColorSchemes.darkDefault, result)
    }

    @Test
    fun mapToDarkColorScheme_givenAllNullSwatches_whenCalled_thenReturnsDarkDefault() {
        val result = mapToDarkColorScheme(BlendedPalette())

        assertEquals(DefaultColorSchemes.darkDefault, result)
    }

    @Test
    fun mapToDarkColorScheme_givenLowContrastOnPrimary_whenCalled_thenOnPrimaryIsAdjustedToPassAA() {
        val palette = buildValidPalette(lightVibrantHex = "#888888", lightVibrantTitle = "#AAAAAA")

        val result = mapToDarkColorScheme(palette)

        assertTrue(contrastRatio(result.onPrimary.toHex(), "#888888") >= 4.5)
    }

    @Test
    fun mapToDarkColorScheme_givenLowContrastOnSurface_whenCalled_thenOnSurfacePassesAAVsBothSurfaceAndBackground() {
        val palette = buildValidPalette(darkMutedHex = "#1A1A2E", lightMutedTitle = "#000000")
        val surfaceHex = compositeColorOverBackground("#1A1A2E", "#000000", 0.40)
        val backgroundHex = compositeColorOverBackground("#1A1A2E", "#000000", 0.20)

        val result = mapToDarkColorScheme(palette)

        val onSurfaceHex = result.onSurface.toHex()
        assertTrue(contrastRatio(onSurfaceHex, surfaceHex) >= 4.5)
        assertTrue(contrastRatio(onSurfaceHex, backgroundHex) >= 4.5)
    }

    @Test
    fun mapToDarkColorScheme_givenValidPalette_whenCalled_thenSurfaceIsDarkMutedDarkened60Percent() {
        val palette = buildValidPalette(darkMutedHex = "#37474F")

        val result = mapToDarkColorScheme(palette)

        val expected = compositeColorOverBackground("#37474F", "#000000", 0.40).toColor()
        assertEquals(expected, result.surface)
    }

    @Test
    fun mapToDarkColorScheme_givenValidPalette_whenCalled_thenBackgroundIsDarkMutedDarkened80Percent() {
        val palette = buildValidPalette(darkMutedHex = "#37474F")

        val result = mapToDarkColorScheme(palette)

        val expected = compositeColorOverBackground("#37474F", "#000000", 0.20).toColor()
        assertEquals(expected, result.background)
    }

    @Test
    fun mapToDarkColorScheme_givenValidPalette_whenCalled_thenOutlineIsCompositedMutedAt30OverDerivedSurface() {
        val palette = buildValidPalette(darkMutedHex = "#37474F", mutedHex = "#607D8B")
        val surfaceHex = compositeColorOverBackground("#37474F", "#000000", 0.40)

        val result = mapToDarkColorScheme(palette)

        val expected = compositeColorOverBackground("#607D8B", surfaceHex, 0.30).toColor()
        assertEquals(expected, result.outline)
    }

    // --- helpers ---

    private fun buildValidPalette(
        vibrantHex: String = "#6200EE",
        vibrantTitle: String = "#FFFFFF",
        darkVibrantHex: String = "#3700B3",
        lightVibrantHex: String = "#BB86FC",
        lightVibrantTitle: String = "#000000",
        mutedHex: String = "#607D8B",
        mutedTitle: String = "#FFFFFF",
        darkMutedHex: String = "#37474F",
        lightMutedHex: String = "#90A4AE",
        lightMutedTitle: String = "#000000",
    ) = BlendedPalette(
        vibrant = SwatchData(hex = vibrantHex, titleText = vibrantTitle),
        darkVibrant = SwatchData(hex = darkVibrantHex, titleText = "#FFFFFF"),
        lightVibrant = SwatchData(hex = lightVibrantHex, titleText = lightVibrantTitle),
        muted = SwatchData(hex = mutedHex, titleText = mutedTitle),
        darkMuted = SwatchData(hex = darkMutedHex, titleText = "#FFFFFF"),
        lightMuted = SwatchData(hex = lightMutedHex, titleText = lightMutedTitle),
    )
}
