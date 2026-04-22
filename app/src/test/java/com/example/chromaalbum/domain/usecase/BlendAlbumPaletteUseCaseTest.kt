package com.example.chromaalbum.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.example.chromaalbum.domain.model.BlendedPalette
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.chromaalbum.domain.model.SwatchData
import com.example.chromaalbum.domain.palette.PaletteCompleter
import com.example.chromaalbum.domain.palette.PaletteEngine
import com.example.chromaalbum.domain.palette.PhotoLoader
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException

@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)
class BlendAlbumPaletteUseCaseTest {
    private val stubBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private val blendedPalette = BlendedPalette(
        vibrant = SwatchData("#FF0000", "#FFFFFF"),
        darkVibrant = null, lightVibrant = null, muted = null, darkMuted = null, lightMuted = null,
    )
    private val completedPalette = BlendedPalette(
        vibrant = SwatchData("#FF1111", "#FFFFFF"),
        darkVibrant = null, lightVibrant = null, muted = null, darkMuted = null, lightMuted = null,
    )

    private val successLoader = object : PhotoLoader {
        override suspend fun load(uri: Uri): Bitmap = stubBitmap
    }
    private val failLoader = object : PhotoLoader {
        override suspend fun load(uri: Uri): Bitmap = throw IOException("load failed")
    }
    private val engine = object : PaletteEngine {
        override suspend fun extractPalette(bitmap: Bitmap) = blendedPalette
        override suspend fun blendPalettes(bitmaps: List<Bitmap>): BlendedPalette? =
            if (bitmaps.isEmpty()) null else blendedPalette
    }
    private val completer = object : PaletteCompleter {
        override fun complete(palette: BlendedPalette) = completedPalette
    }

    @Test
    fun invoke_givenAllUrisFailToLoad_thenReturnsNull() =
        runTest {
            val useCase = BlendAlbumPaletteUseCase(failLoader, engine, completer)

            val result = useCase(listOf(Uri.parse("content://fail1"), Uri.parse("content://fail2")))

            assertNull(result)
        }

    @Test
    fun invoke_givenSuccessfulLoads_thenReturnsPaletteCompleterOutput() =
        runTest {
            val useCase = BlendAlbumPaletteUseCase(successLoader, engine, completer)

            val result = useCase(listOf(Uri.parse("content://a"), Uri.parse("content://b")))

            assertSame(completedPalette, result)
        }

    @Test
    fun invoke_givenSomeUrisFailToLoad_thenSkipsFailedAndBlendsRest() =
        runTest {
            val goodUri = Uri.parse("content://good")
            val badUri = Uri.parse("content://bad")
            val partialLoader = object : PhotoLoader {
                override suspend fun load(uri: Uri): Bitmap =
                    if (uri == goodUri) stubBitmap else throw IOException("bad")
            }
            var bitmapsReceived = 0
            val countingEngine = object : PaletteEngine {
                override suspend fun extractPalette(bitmap: Bitmap) = blendedPalette
                override suspend fun blendPalettes(bitmaps: List<Bitmap>): BlendedPalette? {
                    bitmapsReceived = bitmaps.size
                    return if (bitmaps.isEmpty()) null else blendedPalette
                }
            }
            val useCase = BlendAlbumPaletteUseCase(partialLoader, countingEngine, completer)

            useCase(listOf(badUri, goodUri))

            assertEquals(1, bitmapsReceived)
        }

    @Test
    fun invoke_givenMoreThan50Uris_thenSamples50EvenlySpaced() =
        runTest {
            val uris = (0..59).map { Uri.parse("content://$it") }
            var bitmapsReceived = 0
            val countingEngine = object : PaletteEngine {
                override suspend fun extractPalette(bitmap: Bitmap) = blendedPalette
                override suspend fun blendPalettes(bitmaps: List<Bitmap>): BlendedPalette? {
                    bitmapsReceived = bitmaps.size
                    return blendedPalette
                }
            }
            val useCase = BlendAlbumPaletteUseCase(successLoader, countingEngine, completer)

            useCase(uris)

            assertEquals(50, bitmapsReceived)
        }

    @Test
    fun invoke_givenEmptyList_thenReturnsNull() =
        runTest {
            val useCase = BlendAlbumPaletteUseCase(successLoader, engine, completer)

            val result = useCase(emptyList())

            assertNull(result)
        }
}
