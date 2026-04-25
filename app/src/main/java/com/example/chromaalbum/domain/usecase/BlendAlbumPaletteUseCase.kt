package com.example.chromaalbum.domain.usecase

import android.graphics.Color
import android.net.Uri
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.SwatchData
import com.example.chromaalbum.domain.palette.PaletteCompleter
import com.example.chromaalbum.domain.palette.PaletteEngine
import com.example.chromaalbum.domain.palette.PhotoLoader
import com.example.chromaalbum.domain.palette.hexToRgb
import com.example.chromaalbum.domain.palette.linearise
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import javax.inject.Inject
import kotlin.math.roundToInt

private const val MAX_PHOTOS = 50
private const val BATCH_SIZE = 10

class BlendAlbumPaletteUseCase
    @Inject
    constructor(
        private val photoLoader: PhotoLoader,
        private val paletteEngine: PaletteEngine,
        private val paletteCompleter: PaletteCompleter,
    ) {
        suspend operator fun invoke(uris: List<Uri>): BlendedPalette? {
            val sampled = sample(uris)
            val batchPalettes = mutableListOf<BlendedPalette>()
            for (chunk in sampled.chunked(BATCH_SIZE)) {
                currentCoroutineContext().ensureActive()
                val bitmaps =
                    chunk.mapNotNull { uri ->
                        try {
                            photoLoader.load(uri)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            null
                        }
                    }
                val blended =
                    try {
                        paletteEngine.blendPalettes(bitmaps)
                    } finally {
                        bitmaps.forEach { it.recycle() }
                    }
                if (blended != null) batchPalettes.add(blended)
            }
            val merged = mergeBatchPalettes(batchPalettes) ?: return null
            return paletteCompleter.complete(merged)
        }

        private fun sample(uris: List<Uri>): List<Uri> {
            if (uris.size <= MAX_PHOTOS) return uris
            val step = uris.size.toDouble() / MAX_PHOTOS
            return List(MAX_PHOTOS) { i -> uris[(i * step).toInt()] }
        }

        // TODO: each batch gets equal weight regardless of photo count; revisit if population-weighted cross-batch merging is needed
        private fun mergeBatchPalettes(palettes: List<BlendedPalette>): BlendedPalette? {
            if (palettes.isEmpty()) return null

            fun mergeSwatches(swatches: List<SwatchData?>): SwatchData? {
                val nonNull = swatches.filterNotNull()
                if (nonNull.isEmpty()) return null
                var rSum = 0.0
                var gSum = 0.0
                var bSum = 0.0
                for (swatch in nonNull) {
                    val rgb = hexToRgb(swatch.hex)
                    rSum += Color.red(rgb).toDouble()
                    gSum += Color.green(rgb).toDouble()
                    bSum += Color.blue(rgb).toDouble()
                }
                val count = nonNull.size.toDouble()
                val r = rSum / count
                val g = gSum / count
                val b = bSum / count
                val argb = (r.roundToInt() shl 16) or (g.roundToInt() shl 8) or b.roundToInt()
                val lum =
                    0.2126 * linearise(r / 255.0) +
                        0.7152 * linearise(g / 255.0) +
                        0.0722 * linearise(b / 255.0)
                val titleText = if (lum < 0.1791) "#FFFFFF" else "#000000"
                return SwatchData(hex = "#%06X".format(argb and 0xFFFFFF), titleText = titleText)
            }

            val merged =
                BlendedPalette(
                    vibrant = mergeSwatches(palettes.map { it.vibrant }),
                    darkVibrant = mergeSwatches(palettes.map { it.darkVibrant }),
                    lightVibrant = mergeSwatches(palettes.map { it.lightVibrant }),
                    muted = mergeSwatches(palettes.map { it.muted }),
                    darkMuted = mergeSwatches(palettes.map { it.darkMuted }),
                    lightMuted = mergeSwatches(palettes.map { it.lightMuted }),
                )
            return if (merged.isEmpty()) null else merged
        }
    }
