package com.example.chromaalbum.domain.usecase

import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.model.SwatchData
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)
class SaveEditedAlbumUseCaseTest {

    private val palette = BlendedPalette(vibrant = SwatchData("#FF0000", "#FFFFFF"))

    private class FakeRepo(
        private val album: Album? = defaultAlbum(),
    ) : AlbumRepository {
        val persistedPalettes = mutableListOf<Pair<Long, BlendedPalette>>()
        var addPhotosCallCount = 0

        override fun getAlbumById(albumId: Long): Flow<Album?> = flowOf(album)
        override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = flowOf(emptyList())
        override suspend fun updateAlbum(album: Album) = Unit
        override suspend fun addPhotos(photos: List<Photo>) {
            addPhotosCallCount++
        }
        override suspend fun persistPalette(albumId: Long, palette: BlendedPalette) {
            persistedPalettes += albumId to palette
        }

        override fun getAllAlbums(): Flow<List<Album>> = error("unused")
        override suspend fun createAlbum(name: String, description: String?): Long = error("unused")
        override suspend fun deleteAlbum(album: Album): Unit = error("unused")
        override suspend fun removePhoto(photo: Photo): Unit = error("unused")
    }

    private fun makeUseCase(
        repo: FakeRepo,
        blendResult: BlendedPalette? = palette,
        blendCallCount: IntArray = IntArray(1),
    ) = SaveEditedAlbumUseCase(
        updateAlbumUseCase = UpdateAlbumUseCase(repo),
        addPhotosUseCase = AddPhotosUseCase(repo),
        blendFn = { _ -> blendCallCount[0]++; blendResult },
        repository = repo,
    )

    @Test
    fun invoke_givenNewUris_whenAllSucceed_thenAddsPhotosAndBlendsAndEmitsSuccess() =
        runTest {
            val repo = FakeRepo()
            val blendCallCount = IntArray(1)
            val useCase = makeUseCase(repo, blendCallCount = blendCallCount)
            val newUris = listOf("content://photo/1", "content://photo/2")

            val results = useCase(1L, "Name", null, newUris, newUris).toList()

            assertEquals(1, results.size)
            assertEquals(Result.Success(Unit), results.first())
            assertEquals(1, repo.addPhotosCallCount)
            assertEquals(1, blendCallCount[0])
            assertEquals(1, repo.persistedPalettes.size)
            assertEquals(1L, repo.persistedPalettes.first().first)
        }

    @Test
    fun invoke_givenNoNewUris_whenUpdateSucceeds_thenSkipsAddAndBlendAndEmitsSuccess() =
        runTest {
            val repo = FakeRepo()
            val blendCallCount = IntArray(1)
            val useCase = makeUseCase(repo, blendCallCount = blendCallCount)

            val results = useCase(1L, "Name", null, emptyList(), listOf("content://photo/1")).toList()

            assertEquals(Result.Success(Unit), results.first())
            assertEquals(0, repo.addPhotosCallCount)
            assertEquals(0, blendCallCount[0])
            assertTrue(repo.persistedPalettes.isEmpty())
        }

    @Test
    fun invoke_givenUpdateThrows_whenErrorOccurs_thenEmitsFailure() =
        runTest {
            // null album → UpdateAlbumUseCase emits Failure("Album 99 not found")
            val repo = FakeRepo(album = null)
            val blendCallCount = IntArray(1)
            val useCase = makeUseCase(repo, blendCallCount = blendCallCount)

            val results =
                useCase(99L, "Name", null, listOf("content://photo/1"), listOf("content://photo/1")).toList()

            assertEquals(1, results.size)
            assertTrue(results.first() is Result.Failure<*>)
            assertEquals(0, repo.addPhotosCallCount)
            assertEquals(0, blendCallCount[0])
        }

    @Test
    fun invoke_givenBlendReturnsNull_whenNewPhotosPresent_thenDoesNotPersistPalette() =
        runTest {
            val repo = FakeRepo()
            val useCase = makeUseCase(repo, blendResult = null)
            val newUris = listOf("content://photo/1")

            val results = useCase(1L, "Name", null, newUris, newUris).toList()

            assertEquals(Result.Success(Unit), results.first())
            assertEquals(1, repo.addPhotosCallCount)
            assertTrue(repo.persistedPalettes.isEmpty())
        }

    // region — helpers

    companion object {
        private fun defaultAlbum(id: Long = 1L) =
            Album(id = id, name = "Test", description = null, createdAt = 0L, updatedAt = 0L, coverPhotoUri = null)
    }

    // endregion
}
