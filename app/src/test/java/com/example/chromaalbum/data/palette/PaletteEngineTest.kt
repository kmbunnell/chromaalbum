package com.example.chromaalbum.data.palette

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)
class PaletteEngineTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var engine: PaletteEngineImpl

    @Before
    fun setUp() {
        engine = PaletteEngineImpl(dispatcher)
    }

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
            // A 1×1 fully transparent bitmap yields no Palette swatches.
            val bitmap =
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
                    setPixel(0, 0, Color.TRANSPARENT)
                }

            val result = engine.extractPalette(bitmap)

            // With no swatches and all fallbacks null, every category is null
            assertNull(result.vibrant)
            assertNull(result.darkVibrant)
            assertNull(result.lightVibrant)
            assertNull(result.muted)
            assertNull(result.darkMuted)
            assertNull(result.lightMuted)
        }

    private fun solidBitmap(color: Int): Bitmap {
        val bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(color)
        return bmp
    }
}
