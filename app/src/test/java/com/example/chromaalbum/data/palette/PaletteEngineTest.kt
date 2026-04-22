package com.example.chromaalbum.data.palette

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)

class PaletteEngineTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val engine = PaletteEngineImpl(dispatcher)

    @Test
    fun extractPalette_givenSwatch_thenSwatchDataHasWellFormedHexAndTitleText() =
        runTest(dispatcher) {
            val bitmap = solidBitmap(Color.RED)

            val result = engine.extractPalette(bitmap)

            val swatchData =
                result.vibrant ?: result.darkVibrant ?: result.lightVibrant
                    ?: result.muted ?: result.darkMuted ?: result.lightMuted
            assertNotNull("expected at least one swatch from solid bitmap", swatchData)
            swatchData!!

            fun assertWellFormedHex(value: String) {
                assertEquals(7, value.length)
                assertEquals('#', value[0])
                val parsed = value.removePrefix("#").toLong(16).toInt()
                assertEquals(value, "#%06X".format(parsed and 0xFFFFFF))
            }
            assertWellFormedHex(swatchData.hex)
            assertWellFormedHex(swatchData.titleText)
        }

    @Test
    fun extractPalette_givenNullSwatch_thenSwatchDataIsNull() =
        runTest(dispatcher) {
            val bitmap =
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
                    setPixel(0, 0, Color.TRANSPARENT)
                }

            val result = engine.extractPalette(bitmap)

            assertNull(result.vibrant)
            assertNull(result.darkVibrant)
            assertNull(result.lightVibrant)
            assertNull(result.muted)
            assertNull(result.darkMuted)
            assertNull(result.lightMuted)
        }

    @Test
    fun blendPalettes_givenMultipleIdenticalColorBitmaps_thenBlendedSwatchReflectsThatColor() =
        runTest(dispatcher) {
            val bitmaps = listOf(solidBitmap(Color.RED), solidBitmap(Color.RED))

            val result = engine.blendPalettes(bitmaps)

            assertNotNull("expected non-null result from solid red bitmaps", result)
            val swatch = with(result!!) { vibrant ?: darkVibrant ?: lightVibrant ?: muted ?: darkMuted ?: lightMuted }
            assertNotNull("expected at least one swatch", swatch)
            val rgb = swatch!!.hex.removePrefix("#").toLong(16).toInt()
            val r = (rgb shr 16) and 0xFF
            val g = (rgb shr 8) and 0xFF
            val b = rgb and 0xFF
            assertTrue("red channel must dominate blended swatch from red bitmaps", r > g && r > b)
        }

    @Test
    fun blendPalettes_givenEmptyList_thenReturnsNull() =
        runTest(dispatcher) {
            val result = engine.blendPalettes(emptyList())

            assertNull("empty bitmap list should return null", result)
        }

    private fun solidBitmap(color: Int): Bitmap {
        val bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(color)
        return bmp
    }
}
