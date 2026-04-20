package com.example.chromaalbum.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BlendedPaletteTest {
    private val swatch = SwatchData(hex = "#AABBCC", titleText = "#FFFFFF")

    @Test
    fun isEmpty_givenAllNullCategories_thenReturnsTrue() {
        val palette =
            BlendedPalette(
                vibrant = null,
                darkVibrant = null,
                lightVibrant = null,
                muted = null,
                darkMuted = null,
                lightMuted = null,
            )

        assertTrue(palette.isEmpty())
    }

    @Test
    fun dominantHex_givenVibrantPresent_whenCalled_thenReturnsVibrantHex() {
        val palette = BlendedPalette(
            vibrant = SwatchData(hex = "#FF0000", titleText = "#FFFFFF"),
            darkVibrant = SwatchData(hex = "#AA0000", titleText = "#FFFFFF"),
            lightVibrant = null, muted = null, darkMuted = null, lightMuted = null,
        )

        assertEquals("#FF0000", palette.dominantHex())
    }

    @Test
    fun dominantHex_givenVibrantNull_whenCalled_thenReturnsFirstNonNullFallbackHex() {
        val palette = BlendedPalette(
            vibrant = null,
            darkVibrant = null,
            lightVibrant = null,
            muted = SwatchData(hex = "#CC5555", titleText = "#FFFFFF"),
            darkMuted = null, lightMuted = null,
        )

        assertEquals("#CC5555", palette.dominantHex())
    }

    @Test
    fun dominantHex_givenAllSwatchesNull_whenCalled_thenReturnsNull() {
        val palette = BlendedPalette(
            vibrant = null, darkVibrant = null, lightVibrant = null,
            muted = null, darkMuted = null, lightMuted = null,
        )

        assertNull(palette.dominantHex())
    }

    @Test
    fun isEmpty_givenOneNonNullCategory_thenReturnsFalse() {
        val palette =
            BlendedPalette(
                vibrant = swatch,
                darkVibrant = null,
                lightVibrant = null,
                muted = null,
                darkMuted = null,
                lightMuted = null,
            )

        assertFalse(palette.isEmpty())
    }
}
