package com.example.chromaalbum.domain.usecase

import android.net.Uri
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.palette.PaletteCompleter
import com.example.chromaalbum.domain.palette.PaletteEngine
import com.example.chromaalbum.domain.palette.PhotoLoader
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

private const val MAX_PHOTOS = 50

class BlendAlbumPaletteUseCase
    @Inject
    constructor(
        private val photoLoader: PhotoLoader,
        private val paletteEngine: PaletteEngine,
        private val paletteCompleter: PaletteCompleter,
    ) {
        suspend operator fun invoke(uris: List<Uri>): BlendedPalette? {
            val sampled = sample(uris)
            val bitmaps =
                sampled.mapNotNull { uri ->
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
                    paletteEngine.blendPalettes(bitmaps) ?: return null
                } finally {
                    bitmaps.forEach { it.recycle() }
                }
            return paletteCompleter.complete(blended)
        }

        private fun sample(uris: List<Uri>): List<Uri> {
            if (uris.size <= MAX_PHOTOS) return uris
            val step = uris.size.toDouble() / MAX_PHOTOS
            return List(MAX_PHOTOS) { i -> uris[(i * step).toInt()] }
        }
    }
