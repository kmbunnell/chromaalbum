package com.example.chromaalbum.data.palette

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.core.graphics.scale
import androidx.palette.graphics.Palette
import com.example.chromaalbum.di.DefaultDispatcher
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.SwatchData
import com.example.chromaalbum.domain.palette.PaletteEngine
import com.example.chromaalbum.domain.palette.PhotoLoader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException

class PaletteEngineImpl
    @Inject
    constructor(
        @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
        private val photoLoader: PhotoLoader,
    ) : PaletteEngine {
        override suspend fun extractPalette(bitmap: Bitmap): BlendedPalette =
            withContext(dispatcher) {
                val scaled = bitmap.scale(100, 100)
                val palette = extractRawPalette(scaled)
                if (scaled !== bitmap) scaled.recycle()
                blendedPaletteFrom(palette)
            }

        override suspend fun blendPalettes(uris: List<Uri>): BlendedPalette? =
            withContext(dispatcher) {
                val palettes = uris.mapNotNull { uri ->
                    try {
                        val bitmap = photoLoader.load(uri)
                        val palette = extractRawPalette(bitmap)
                        bitmap.recycle()
                        palette
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        null
                    }
                }
                if (palettes.isEmpty()) return@withContext null

                BlendedPalette(
                    vibrant = weightedAvgSwatch(palettes.mapNotNull { it.vibrantSwatch }),
                    darkVibrant = weightedAvgSwatch(palettes.mapNotNull { it.darkVibrantSwatch }),
                    lightVibrant = weightedAvgSwatch(palettes.mapNotNull { it.lightVibrantSwatch }),
                    muted = weightedAvgSwatch(palettes.mapNotNull { it.mutedSwatch }),
                    darkMuted = weightedAvgSwatch(palettes.mapNotNull { it.darkMutedSwatch }),
                    lightMuted = weightedAvgSwatch(palettes.mapNotNull { it.lightMutedSwatch }),
                )
            }

        private fun weightedAvgSwatch(swatches: List<Palette.Swatch>): SwatchData? {
            if (swatches.isEmpty()) return null
            val totalPop = swatches.sumOf { it.population }.takeIf { it > 0 } ?: return null
            val r = swatches.sumOf { Color.red(it.rgb) * it.population }.toDouble() / totalPop
            val g = swatches.sumOf { Color.green(it.rgb) * it.population }.toDouble() / totalPop
            val b = swatches.sumOf { Color.blue(it.rgb) * it.population }.toDouble() / totalPop
            val rgb = (r.roundToInt() shl 16) or (g.roundToInt() shl 8) or b.roundToInt()
            val luminance = 0.2126 * linearise(r / 255.0) + 0.7152 * linearise(g / 255.0) + 0.0722 * linearise(b / 255.0)
            val titleText = if (luminance < 0.1791) "#FFFFFF" else "#000000"
            return SwatchData(hex = "#%06X".format(rgb and 0xFFFFFF), titleText = titleText)
        }

        private fun extractRawPalette(bitmap: Bitmap): Palette = Palette.from(bitmap).generate()

        private fun blendedPaletteFrom(palette: Palette) =
            BlendedPalette(
                vibrant = palette.vibrantSwatch?.toSwatchData(),
                darkVibrant = palette.darkVibrantSwatch?.toSwatchData(),
                lightVibrant = palette.lightVibrantSwatch?.toSwatchData(),
                muted = palette.mutedSwatch?.toSwatchData(),
                darkMuted = palette.darkMutedSwatch?.toSwatchData(),
                lightMuted = palette.lightMutedSwatch?.toSwatchData(),
            )
    }

private fun linearise(c: Double) = if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)

private fun Palette.Swatch.toSwatchData() =
    SwatchData(
        hex = "#%06X".format(rgb and 0xFFFFFF),
        titleText = "#%06X".format(titleTextColor and 0xFFFFFF),
    )
