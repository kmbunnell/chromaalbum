package com.example.chromaalbum.domain.usecase

import android.net.Uri
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.model.SwatchData
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)
class AutoRegeneratePaletteUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()

    // ---- fakes ----

    private class FakeRepo : AlbumRepository {
        val photoFlow = MutableSharedFlow<List<Photo>>(replay = 1)
        val persistedPalettes = mutableListOf<Pair<Long, BlendedPalette>>()

        override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = photoFlow

        override suspend fun persistPalette(albumId: Long, palette: BlendedPalette) {
            persistedPalettes += albumId to palette
        }

        override fun getAllAlbums(): Flow<List<Album>> = error("unused")
        override fun getAlbumById(albumId: Long): Flow<Album?> = error("unused")
        override suspend fun createAlbum(name: String, description: String?): Long = error("unused")
        override suspend fun updateAlbum(album: Album): Unit = error("unused")
        override suspend fun deleteAlbum(album: Album): Unit = error("unused")
        override suspend fun addPhotos(photos: List<Photo>): Unit = error("unused")
        override suspend fun removePhoto(photo: Photo): Unit = error("unused")
    }

    private class FakeBlendUseCase(
        private var result: BlendedPalette? = BlendedPalette(vibrant = SwatchData("#FF0000", "#FFFFFF")),
        private val gate: CompletableDeferred<Unit>? = null,
    ) {
        var invokeCount = 0

        suspend operator fun invoke(uris: List<Uri>): BlendedPalette? {
            gate?.await()
            invokeCount++
            return result
        }
    }

    // ---- helpers ----

    private fun photos(count: Int, prefix: String = "content://p"): List<Photo> =
        List(count) { i -> Photo(albumId = 1L, uri = "$prefix/$i", addedAt = 0L, sortOrder = i) }

    private fun makeUseCase(
        repo: FakeRepo,
        blend: FakeBlendUseCase,
    ) = AutoRegeneratePaletteUseCase(repo, blend::invoke, testDispatcher, debounceMs = 2_000L)

    // ---- tests ----

    @Test
    fun invoke_givenRapidPhotoListChanges_whenWithinDebounceWindow_thenBlendCalledExactlyOnce() =
        runTest(testDispatcher) {
            val repo = FakeRepo()
            val blend = FakeBlendUseCase()
            val useCase = makeUseCase(repo, blend)

            val job = launch { useCase(1L).collect {} }

            repeat(20) { i ->
                repo.photoFlow.emit(photos(i + 1))
                advanceTimeBy(100L)
            }
            advanceTimeBy(2_001L)

            assertEquals(1, blend.invokeCount)
            job.cancel()
        }

    @Test
    fun invoke_givenPhotoListChange_whenDebounceExpires_thenPersistPaletteIsCalled() =
        runTest(testDispatcher) {
            val repo = FakeRepo()
            val expectedPalette = BlendedPalette(vibrant = SwatchData("#FF0000", "#FFFFFF"))
            val blend = FakeBlendUseCase(result = expectedPalette)
            val useCase = makeUseCase(repo, blend)

            val job = launch { useCase(1L).collect {} }

            repo.photoFlow.emit(photos(3))
            advanceTimeBy(2_001L)

            assertEquals(1, repo.persistedPalettes.size)
            assertEquals(expectedPalette, repo.persistedPalettes.first().second)
            job.cancel()
        }

    @Test
    fun invoke_givenAllPhotosRemoved_whenDebounceExpires_thenPersistsEmptyPalette() =
        runTest(testDispatcher) {
            val repo = FakeRepo()
            val blend = FakeBlendUseCase()
            val useCase = makeUseCase(repo, blend)

            val job = launch { useCase(1L).collect {} }

            repo.photoFlow.emit(emptyList())
            advanceTimeBy(2_001L)

            assertEquals(1, repo.persistedPalettes.size)
            assertEquals(BlendedPalette(), repo.persistedPalettes.first().second)
            assertEquals(0, blend.invokeCount)
            job.cancel()
        }

    @Test
    fun invoke_givenBlendReturnsNull_whenDebounceExpires_thenPersistPaletteNotCalled() =
        runTest(testDispatcher) {
            val repo = FakeRepo()
            val blend = FakeBlendUseCase(result = null)
            val useCase = makeUseCase(repo, blend)

            val job = launch { useCase(1L).collect {} }

            repo.photoFlow.emit(photos(3))
            advanceTimeBy(2_001L)

            assertEquals(0, repo.persistedPalettes.size)
            job.cancel()
        }

    @Test
    fun invoke_givenSlowInFlightRegen_whenNewDebounceWindowCloses_thenOnlyLatestResultIsPersisted() =
        runTest(testDispatcher) {
            val repo = FakeRepo()
            val firstGate = CompletableDeferred<Unit>()
            val firstPalette = BlendedPalette(vibrant = SwatchData("#111111", "#FFFFFF"))
            val secondPalette = BlendedPalette(vibrant = SwatchData("#222222", "#FFFFFF"))
            var callIndex = 0
            val blend =
                object {
                    suspend operator fun invoke(uris: List<Uri>): BlendedPalette? {
                        val idx = callIndex++
                        if (idx == 0) firstGate.await()
                        return if (idx == 0) firstPalette else secondPalette
                    }
                }
            val useCase = AutoRegeneratePaletteUseCase(repo, blend::invoke, testDispatcher, debounceMs = 2_000L)

            val job = launch { useCase(1L).collect {} }

            repo.photoFlow.emit(photos(1, "content://a"))
            advanceTimeBy(2_001L)

            repo.photoFlow.emit(photos(2, "content://b"))
            advanceTimeBy(2_001L)

            firstGate.complete(Unit)
            advanceTimeBy(100L)

            assertNull(repo.persistedPalettes.firstOrNull { it.second == firstPalette })
            assertEquals(1, repo.persistedPalettes.size)
            assertEquals(secondPalette, repo.persistedPalettes.first().second)
            job.cancel()
        }
}
