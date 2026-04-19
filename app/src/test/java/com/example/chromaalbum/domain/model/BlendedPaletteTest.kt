package com.example.chromaalbum.domain.model

import org.junit.Assert.assertFalse
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
