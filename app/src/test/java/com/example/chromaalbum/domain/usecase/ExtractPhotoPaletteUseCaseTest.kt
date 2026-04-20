package com.example.chromaalbum.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.SwatchData
import com.example.chromaalbum.domain.palette.PaletteEngine
import com.example.chromaalbum.domain.palette.PhotoLoader
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)
class ExtractPhotoPaletteUseCaseTest {
    private val testUri = Uri.parse("content://com.example/photos/1")
    private val testBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
    private val testPalette =
        BlendedPalette(
            vibrant = SwatchData("#FF0000", "#FFFFFF"),
            darkVibrant = null,
            lightVibrant = null,
            muted = null,
            darkMuted = null,
            lightMuted = null,
        )

    @Test
    fun invoke_givenValidUri_whenPhotoLoads_thenReturnsPaletteFromEngine() =
        runTest {
            val fakeLoader = FakePhotoLoader(bitmap = testBitmap)
            val fakeEngine = FakePaletteEngine(palette = testPalette)
            val useCase = ExtractPhotoPaletteUseCase(fakeLoader, fakeEngine)

            val result = useCase(testUri)

            assertEquals(testPalette, result)
        }

    @Test
    fun invoke_givenUnreadableUri_whenPhotoLoadFails_thenPropagatesException() =
        runTest {
            val loadError = IllegalStateException("Failed to load photo: $testUri")
            val fakeLoader = FakePhotoLoader(throwable = loadError)
            val fakeEngine = FakePaletteEngine(palette = testPalette)
            val useCase = ExtractPhotoPaletteUseCase(fakeLoader, fakeEngine)

            val exception = runCatching { useCase(testUri) }.exceptionOrNull()

            assertNotNull(exception)
            assertTrue(exception is IllegalStateException)
            assertEquals(loadError.message, exception!!.message)
        }

    private class FakePhotoLoader(
        private val bitmap: Bitmap? = null,
        private val throwable: Throwable? = null,
    ) : PhotoLoader {
        override suspend fun load(uri: Uri): Bitmap = throwable?.let { throw it } ?: bitmap!!
    }

    private class FakePaletteEngine(
        private val palette: BlendedPalette,
    ) : PaletteEngine {
        override suspend fun extractPalette(bitmap: Bitmap): BlendedPalette = palette
    }
}
