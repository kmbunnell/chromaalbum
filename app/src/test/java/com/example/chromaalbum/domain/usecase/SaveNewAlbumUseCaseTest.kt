package com.example.chromaalbum.domain.usecase

import android.net.Uri
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
class SaveNewAlbumUseCaseTest {

    private val palette = BlendedPalette(vibrant = SwatchData("#FF0000", "#FFFFFF"))

    private class FakeRepo(
        private val createdId: Long = 42L,
        private val throwOnAddPhotos: Boolean = false,
    ) : AlbumRepository {
        val persistedPalettes = mutableListOf<Pair<Long, BlendedPalette>>()

        override suspend fun createAlbum(name: String, description: String?): Long = createdId

        override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = flowOf(emptyList())

        override fun getAlbumById(albumId: Long): Flow<Album?> =
            flowOf(Album(id = albumId, name = "Test", description = null, createdAt = 0L, updatedAt = 0L, coverPhotoUri = null))

        override suspend fun addPhotos(photos: List<Photo>) {
            if (throwOnAddPhotos) throw RuntimeException("storage full")
        }

        override suspend fun updateAlbum(album: Album) = Unit

        override suspend fun persistPalette(albumId: Long, palette: BlendedPalette) {
            persistedPalettes += albumId to palette
        }

        override fun getAllAlbums(): Flow<List<Album>> = error("unused")
        override suspend fun deleteAlbum(album: Album): Unit = error("unused")
        override suspend fun removePhoto(photo: Photo): Unit = error("unused")
    }

    private fun makeUseCase(
        repo: FakeRepo,
        blendResult: BlendedPalette? = palette,
        blendCallCount: IntArray = IntArray(1),
    ): SaveNewAlbumUseCase {
        val blendFn: suspend (List<Uri>) -> BlendedPalette? = { _ ->
            blendCallCount[0]++
            blendResult
        }
        return SaveNewAlbumUseCase(
            createAlbumUseCase = CreateAlbumUseCase(repo),
            addPhotosUseCase = AddPhotosUseCase(repo),
            blendFn = blendFn,
            repository = repo,
        )
    }

    @Test
    fun invoke_givenNameAndPhotos_whenSuccess_thenEmitsAlbumIdAndPalettePersisted() =
        runTest {
            val repo = FakeRepo(createdId = 42L)
            val useCase = makeUseCase(repo)

            val results =
                useCase("My Album", null, listOf("content://photo/1", "content://photo/2")).toList()

            assertEquals(1, results.size)
            assertEquals(Result.Success(42L), results.first())
            assertEquals(1, repo.persistedPalettes.size)
            assertEquals(42L, repo.persistedPalettes.first().first)
            assertEquals(palette, repo.persistedPalettes.first().second)
        }

    @Test
    fun invoke_givenEmptyUris_whenSuccess_thenAlbumCreatedAndBlendNeverCalled() =
        runTest {
            val repo = FakeRepo(createdId = 42L)
            val blendCallCount = IntArray(1)
            val useCase = makeUseCase(repo, blendCallCount = blendCallCount)

            val results = useCase("My Album", null, emptyList()).toList()

            assertEquals(Result.Success(42L), results.first())
            assertEquals(0, blendCallCount[0])
            assertTrue(repo.persistedPalettes.isEmpty())
        }

    @Test
    fun invoke_givenAddPhotosFails_whenInvoked_thenEmitsFailure() =
        runTest {
            val repo = FakeRepo(throwOnAddPhotos = true)
            val useCase = makeUseCase(repo)

            val results = useCase("My Album", null, listOf("content://photo/1")).toList()

            assertEquals(1, results.size)
            assertTrue(results.first() is Result.Failure)
            assertTrue(repo.persistedPalettes.isEmpty())
        }
}
