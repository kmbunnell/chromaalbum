package com.example.chromaalbum.domain.usecase

import app.cash.turbine.test
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateAlbumUseCaseTest {

    private val baseAlbum =
        Album(
            id = 1L,
            name = "Old Name",
            description = "Old Description",
            createdAt = 0L,
            updatedAt = 0L,
            coverPhotoUri = null,
        )

    @Test
    fun invoke_givenExistingAlbum_whenInvoked_thenCallsUpdateWithTrimmedValues() =
        runTest {
            var updatedAlbum: Album? = null
            val useCase =
                UpdateAlbumUseCase(
                    repoWithGetAndUpdate(
                        albumFlow = flowOf(baseAlbum),
                        onUpdate = { updatedAlbum = it },
                    ),
                )

            useCase(1L, "  New Name  ", "  ").test {
                val item = awaitItem()
                assertTrue(item is Result.Success)
                awaitComplete()
            }

            assertEquals("New Name", updatedAlbum?.name)
            assertNull(updatedAlbum?.description)
        }

    @Test
    fun invoke_givenAlbumNotFound_whenInvoked_thenEmitsFailure() =
        runTest {
            val useCase =
                UpdateAlbumUseCase(
                    repoWithGetAndUpdate(albumFlow = flowOf(null)),
                )

            useCase(99L, "Name", null).test {
                val item = awaitItem()
                assertTrue(item is Result.Failure)
                awaitComplete()
            }
        }

    @Test
    fun invoke_givenRepositoryUpdateThrows_whenInvoked_thenEmitsFailure() =
        runTest {
            val useCase =
                UpdateAlbumUseCase(
                    repoWithGetAndUpdate(
                        albumFlow = flowOf(baseAlbum),
                        onUpdate = { throw RuntimeException("write error") },
                    ),
                )

            useCase(1L, "Name", null).test {
                val item = awaitItem()
                assertTrue(item is Result.Failure)
                assertEquals("write error", (item as Result.Failure).error)
                awaitComplete()
            }
        }

    private fun repoWithGetAndUpdate(
        albumFlow: Flow<Album?>,
        onUpdate: suspend (Album) -> Unit = {},
    ) = object : AlbumRepository {
        override fun getAllAlbums(): Flow<List<Album>> = error("unused")
        override fun getAlbumById(albumId: Long): Flow<Album?> = albumFlow
        override suspend fun createAlbum(name: String, description: String?): Long = error("unused")
        override suspend fun updateAlbum(album: Album) = onUpdate(album)
        override suspend fun deleteAlbum(album: Album) = error("unused")
        override suspend fun addPhotos(photos: List<Photo>) = error("unused")
        override suspend fun removePhoto(photo: Photo) = error("unused")
        override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = error("unused")
        override suspend fun persistPalette(albumId: Long, palette: BlendedPalette) = error("unused")
    }
}
