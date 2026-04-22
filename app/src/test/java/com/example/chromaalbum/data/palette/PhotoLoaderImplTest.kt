package com.example.chromaalbum.data.palette

import android.graphics.Bitmap
import android.net.Uri
import coil3.ImageLoader
import coil3.asImage
import coil3.intercept.Interceptor
import coil3.size.Size
import coil3.test.FakeImageLoaderEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)
class PhotoLoaderImplTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val context get() = RuntimeEnvironment.getApplication()
    private val testUri = Uri.parse("content://com.example/photos/1")

    @Test
    fun load_givenValidUri_whenCalled_thenRequestsDownsampledToHundredByHundred() =
        runTest(dispatcher) {
            var capturedSize: Size? = null
            val engine =
                FakeImageLoaderEngine
                    .Builder()
                    .intercept({ it == testUri }, Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImage())
                    .build()
            val capturer =
                Interceptor { chain ->
                    capturedSize = chain.size
                    chain.proceed()
                }
            val imageLoader =
                ImageLoader
                    .Builder(context)
                    .components {
                        add(capturer)
                        add(engine)
                    }.build()
            val loader = PhotoLoaderImpl(imageLoader, dispatcher, context)

            loader.load(testUri)

            assertEquals(Size(100, 100), capturedSize)
        }

    @Test
    fun load_givenUnreadableUri_whenCoilFails_thenThrowsIllegalStateException() =
        runTest(dispatcher) {
            // FakeImageLoaderEngine with no matching intercept throws → Coil wraps in ErrorResult
            val engine = FakeImageLoaderEngine.Builder().build()
            val imageLoader = ImageLoader.Builder(context).components { add(engine) }.build()
            val loader = PhotoLoaderImpl(imageLoader, dispatcher, context)

            val exception = runCatching { loader.load(testUri) }.exceptionOrNull()

            assertNotNull(exception)
            assertTrue(exception is IllegalStateException)
        }
}
