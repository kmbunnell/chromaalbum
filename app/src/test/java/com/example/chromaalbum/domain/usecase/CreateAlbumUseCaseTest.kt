package com.example.chromaalbum.domain.usecase

import app.cash.turbine.test
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateAlbumUseCaseTest {

    @Test
    fun invoke_givenValidName_whenInvoked_thenTrimsAndEmitsSuccessWithId() =
        runTest {
            var capturedName: String? = null
            val useCase = CreateAlbumUseCase(repoWithCreate { name, _ ->
                capturedName = name
                42L
            })

            useCase("  My Album  ", null).test {
                val item = awaitItem()
                assertTrue(item is Result.Success)
                assertEquals(42L, (item as Result.Success).data)
                assertEquals("My Album", capturedName)
                awaitComplete()
            }
        }

    @Test
    fun invoke_givenDescriptionBlank_whenInvoked_thenDescriptionIsNullInRepository() =
        runTest {
            var capturedDescription: String? = "sentinel"
            val useCase = CreateAlbumUseCase(repoWithCreate { _, desc ->
                capturedDescription = desc
                1L
            })

            useCase("Name", "   ").test {
                awaitItem()
                awaitComplete()
            }

            assertEquals(null, capturedDescription)
        }

    @Test
    fun invoke_givenRepositoryThrows_whenInvoked_thenEmitsFailure() =
        runTest {
            val useCase = CreateAlbumUseCase(repoWithCreate { _, _ -> throw RuntimeException("db error") })

            useCase("Name", null).test {
                val item = awaitItem()
                assertTrue(item is Result.Failure)
                assertEquals("db error", (item as Result.Failure).error)
                awaitComplete()
            }
        }

    @Test
    fun invoke_givenCancellationException_whenInvoked_thenRethrows() =
        runTest {
            val useCase = CreateAlbumUseCase(repoWithCreate { _, _ -> throw CancellationException("cancelled") })

            var caughtCancellation = false
            try {
                useCase("Name", null).collect {}
            } catch (e: CancellationException) {
                caughtCancellation = true
            }

            assertTrue(caughtCancellation)
        }

    private fun repoWithCreate(createAlbum: suspend (String, String?) -> Long) =
        object : AlbumRepository {
            override fun getAllAlbums(): Flow<List<Album>> = error("unused")
            override fun getAlbumById(albumId: Long): Flow<Album?> = error("unused")
            override suspend fun createAlbum(name: String, description: String?) = createAlbum(name, description)
            override suspend fun updateAlbum(album: Album) = error("unused")
            override suspend fun deleteAlbum(album: Album) = error("unused")
            override suspend fun addPhotos(photos: List<Photo>) = error("unused")
            override suspend fun removePhoto(photo: Photo) = error("unused")
            override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = error("unused")
            override suspend fun persistPalette(albumId: Long, palette: BlendedPalette) = error("unused")
        }
}
