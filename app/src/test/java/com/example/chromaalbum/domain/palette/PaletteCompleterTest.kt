package com.example.chromaalbum.domain.palette

import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.SwatchData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PaletteCompleterTest {
    private lateinit var completer: PaletteCompleter

    // H=0, S=1.0, L=0.5 — pure saturated red, yields clean derivation values
    private val redSwatch = SwatchData("#FF0000", "#FFFFFF")

    // H=0, S=1.0, L=0.8 — high-lightness red, triggers 0.95 clamp on lightVibrant derivation
    private val lightRedSwatch = SwatchData("#FF9999", "#000000")

    @Before
    fun setUp() {
        completer = PaletteCompleter()
    }

    @Test
    fun complete_givenEmptyPalette_thenReturnsEmptyPalette() {
        val result = completer.complete(emptyPalette())

        assertTrue(result.isEmpty())
    }

    @Test
    fun complete_givenAllSwatchesPresent_thenAllSwatchesUnchanged() {
        val palette = fullPalette()

        val result = completer.complete(palette)

        assertEquals(palette, result)
    }

    @Test
    fun complete_givenDarkVibrantNull_thenDarkVibrantLightnessIsVibrantLightnessScaledBy0Point6() {
        val palette = fullPalette().copy(darkVibrant = null)

        val result = completer.complete(palette)

        val (_, _, vibrantL) = hexToHsl(palette.vibrant!!.hex)
        val (_, _, darkVibrantL) = hexToHsl(result.darkVibrant!!.hex)
        assertEquals(vibrantL * 0.6f, darkVibrantL, 0.02f)
    }

    @Test
    fun complete_givenLightVibrantNull_thenLightVibrantLightnessIsVibrantLightnessScaledBy1Point4() {
        val palette = fullPalette().copy(lightVibrant = null)

        val result = completer.complete(palette)

        val (_, _, vibrantL) = hexToHsl(palette.vibrant!!.hex)
        val (_, _, lightVibrantL) = hexToHsl(result.lightVibrant!!.hex)
        assertEquals(vibrantL * 1.4f, lightVibrantL, 0.02f)
    }

    @Test
    fun complete_givenHighLightnessVibrantAndLightVibrantNull_thenLightVibrantLightnessClampedTo0Point95() {
        val palette = fullPalette(vibrant = lightRedSwatch).copy(lightVibrant = null)

        val result = completer.complete(palette)

        val (_, _, lightVibrantL) = hexToHsl(result.lightVibrant!!.hex)
        assertEquals(0.95f, lightVibrantL, 0.02f)
    }

    @Test
    fun complete_givenMutedNull_thenMutedSaturationIsVibrantSaturationScaledBy0Point4() {
        val palette = fullPalette().copy(muted = null)

        val result = completer.complete(palette)

        val (_, vibrantS, _) = hexToHsl(palette.vibrant!!.hex)
        val (_, mutedS, _) = hexToHsl(result.muted!!.hex)
        assertEquals(vibrantS * 0.4f, mutedS, 0.02f)
    }

    @Test
    fun complete_givenDarkMutedNull_thenDarkMutedLightnessIsMutedLightnessScaledBy0Point6() {
        val palette = fullPalette().copy(darkMuted = null)

        val result = completer.complete(palette)

        val (_, _, mutedL) = hexToHsl(palette.muted!!.hex)
        val (_, _, darkMutedL) = hexToHsl(result.darkMuted!!.hex)
        assertEquals(mutedL * 0.6f, darkMutedL, 0.02f)
    }

    @Test
    fun complete_givenLightMutedNull_thenLightMutedLightnessIsMutedLightnessScaledBy1Point4() {
        val palette = fullPalette().copy(lightMuted = null)

        val result = completer.complete(palette)

        val (_, _, mutedL) = hexToHsl(palette.muted!!.hex)
        val (_, _, lightMutedL) = hexToHsl(result.lightMuted!!.hex)
        assertEquals(mutedL * 1.4f, lightMutedL, 0.02f)
    }

    @Test
    fun complete_givenDerivedSwatchLightnessBelow0Point5_thenTitleTextIsWhite() {
        // darkVibrant derived from #FF0000 (L=0.5) → derived L=0.3 < 0.5
        val palette = fullPalette().copy(darkVibrant = null)

        val result = completer.complete(palette)

        assertEquals("#FFFFFF", result.darkVibrant!!.titleText)
    }

    @Test
    fun complete_givenDerivedSwatchLightnessAtOrAbove0Point5_thenTitleTextIsBlack() {
        // lightVibrant derived from #FF0000 (L=0.5) → derived L=0.7 >= 0.5
        val palette = fullPalette().copy(lightVibrant = null)

        val result = completer.complete(palette)

        assertEquals("#000000", result.lightVibrant!!.titleText)
    }

    @Test
    fun complete_givenVibrantNull_thenVibrantFilledFromFirstAvailableSwatch() {
        val palette = fullPalette().copy(vibrant = null)

        val result = completer.complete(palette)

        assertEquals(palette.muted, result.vibrant)
    }

    private fun fullPalette(vibrant: SwatchData = redSwatch) =
        BlendedPalette(
            vibrant = vibrant,
            darkVibrant = SwatchData("#990000", "#FFFFFF"),
            lightVibrant = SwatchData("#FF6666", "#000000"),
            muted = SwatchData("#884444", "#FFFFFF"),
            darkMuted = SwatchData("#442222", "#FFFFFF"),
            lightMuted = SwatchData("#CCAAAA", "#000000"),
        )

    private fun emptyPalette() =
        BlendedPalette(
            vibrant = null,
            darkVibrant = null,
            lightVibrant = null,
            muted = null,
            darkMuted = null,
            lightMuted = null,
        )
}
