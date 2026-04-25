package com.example.chromaalbum.domain.palette

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorUtilsTest {

    // relativeLuminance

    @Test
    fun relativeLuminance_givenMidGray_thenReturnsExpectedValue() {
        // #808080 → channel = 128/255 ≈ 0.50196 → linearised ≈ 0.2158
        val lum = relativeLuminance("#808080")
        assertEquals(0.2158, lum, 0.001)
    }

    // contrastRatio

    @Test
    fun contrastRatio_givenWhiteAndBlack_thenReturns21() {
        assertEquals(21.0, contrastRatio("#FFFFFF", "#000000"), 0.01)
    }

    @Test
    fun contrastRatio_givenIdenticalColors_thenReturns1() {
        assertEquals(1.0, contrastRatio("#FF5733", "#FF5733"), 1e-6)
    }

    @Test
    fun contrastRatio_givenArgOrderSwapped_thenReturnsSameRatio() {
        val a = "#FF5733"
        val b = "#003399"
        assertEquals(contrastRatio(a, b), contrastRatio(b, a), 1e-6)
    }

    // compositeColorOverBackground

    @Test
    fun compositeColorOverBackground_givenFullOpacity_thenReturnsForeground() {
        assertEquals("#FF5733", compositeColorOverBackground("#FF5733", "#FFFFFF", 1.0))
    }

    @Test
    fun compositeColorOverBackground_givenZeroOpacity_thenReturnsBackground() {
        assertEquals("#FFFFFF", compositeColorOverBackground("#FF5733", "#FFFFFF", 0.0))
    }

    @Test
    fun compositeColorOverBackground_givenHalfOpacity_thenBlends50Percent() {
        // black (0,0,0) over white (255,255,255) at 0.5 → 128 per channel
        assertEquals("#808080", compositeColorOverBackground("#000000", "#FFFFFF", 0.5))
    }

    // adjustForContrast

    @Test
    fun adjustForContrast_givenAlreadyMeetsThreshold_thenReturnsUnchanged() {
        // black on white → ratio 21 >> 4.5
        assertEquals("#000000", adjustForContrast("#000000", "#FFFFFF"))
    }

    @Test
    fun adjustForContrast_givenFailingContrast_thenReturnedColorMeetsThreshold() {
        // mid-gray on white → contrast < 4.5; result must meet 4.5
        val result = adjustForContrast("#808080", "#FFFFFF")
        assertTrue(contrastRatio(result, "#FFFFFF") >= 4.5)
    }

    @Test
    fun adjustForContrast_givenFailingContrast_thenReturnsAdjustedTextNotBackground() {
        val result = adjustForContrast("#808080", "#FFFFFF")
        assertTrue(result != "#FFFFFF")
    }

    @Test
    fun adjustForContrast_givenCustomMinRatio_thenEnforcesCustomThreshold() {
        val result = adjustForContrast("#808080", "#FFFFFF", minRatio = 7.0)
        assertTrue(contrastRatio(result, "#FFFFFF") >= 7.0)
    }
}
