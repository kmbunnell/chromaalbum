package com.example.chromaalbum.domain.usecase

import app.cash.turbine.test
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetAlbumUseCaseTest {
    @Test
    fun invoke_givenExistingAlbum_whenCollected_thenEmitsSuccess() =
        runTest {
            val sharedFlow = MutableSharedFlow<Album?>(replay = 1)
            val useCase = GetAlbumUseCase(fakeRepo(albumFlow = sharedFlow))
            sharedFlow.emit(album(42))

            useCase(42L).test {
                val item = awaitItem()
                assertTrue(item is Result.Success)
                assertEquals(42L, (item as Result.Success).data.id)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun invoke_givenAlbumNotFound_whenCollected_thenEmitsFailure() =
        runTest {
            val sharedFlow = MutableSharedFlow<Album?>(replay = 1)
            val useCase = GetAlbumUseCase(fakeRepo(albumFlow = sharedFlow))
            sharedFlow.emit(null)

            useCase(99L).test {
                val item = awaitItem()
                assertTrue(item is Result.Failure)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun invoke_givenRepositoryThrows_whenCollected_thenEmitsFailure() =
        runTest {
            val useCase = GetAlbumUseCase(throwingRepo())

            useCase(1L).test {
                val item = awaitItem()
                assertTrue(item is Result.Failure)
                assertEquals("db error", (item as Result.Failure).error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun album(id: Long) =
        Album(id = id, name = "Album $id", description = null, createdAt = 0L, updatedAt = 0L, coverPhotoUri = null)

    private fun fakeRepo(albumFlow: Flow<Album?>) =
        object : AlbumRepository {
            override fun getAllAlbums(): Flow<List<Album>> = error("unused")
            override fun getAlbumById(albumId: Long): Flow<Album?> = albumFlow
            override suspend fun createAlbum(name: String, description: String?) = 0L
            override suspend fun updateAlbum(album: Album) = Unit
            override suspend fun deleteAlbum(album: Album) = Unit
            override suspend fun addPhotos(photos: List<Photo>) = error("unused")
            override suspend fun removePhoto(photo: Photo) = error("unused")
            override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = error("unused")
            override suspend fun persistPalette(albumId: Long, palette: BlendedPalette) = error("unused")
        }

    private fun throwingRepo() =
        object : AlbumRepository {
            override fun getAllAlbums(): Flow<List<Album>> = error("unused")
            override fun getAlbumById(albumId: Long): Flow<Album?> = throw RuntimeException("db error")
            override suspend fun createAlbum(name: String, description: String?) = 0L
            override suspend fun updateAlbum(album: Album) = Unit
            override suspend fun deleteAlbum(album: Album) = Unit
            override suspend fun addPhotos(photos: List<Photo>) = error("unused")
            override suspend fun removePhoto(photo: Photo) = error("unused")
            override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = error("unused")
            override suspend fun persistPalette(albumId: Long, palette: BlendedPalette) = error("unused")
        }
}
