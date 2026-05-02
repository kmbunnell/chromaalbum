package com.example.chromaalbum.domain.usecase

import app.cash.turbine.test
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetPhotosForAlbumUseCaseTest {
    @Test
    fun invoke_givenPhotosExist_whenCollected_thenEmitsSuccess() =
        runTest {
            val sharedFlow = MutableSharedFlow<List<Photo>>(replay = 1)
            val useCase = GetPhotosForAlbumUseCase(fakeRepo(photosFlow = sharedFlow))
            sharedFlow.emit(listOf(photo(1), photo(2), photo(3)))

            useCase(1L).test {
                val item = awaitItem()
                assertTrue(item is Result.Success)
                assertEquals(3, (item as Result.Success).data.size)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun invoke_givenNoPhotos_whenCollected_thenEmitsSuccessWithEmptyList() =
        runTest {
            val sharedFlow = MutableSharedFlow<List<Photo>>(replay = 1)
            val useCase = GetPhotosForAlbumUseCase(fakeRepo(photosFlow = sharedFlow))
            sharedFlow.emit(emptyList())

            useCase(1L).test {
                val item = awaitItem()
                assertTrue(item is Result.Success)
                assertTrue((item as Result.Success).data.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun invoke_givenRepositoryThrows_whenCollected_thenEmitsFailure() =
        runTest {
            val useCase = GetPhotosForAlbumUseCase(throwingRepo())

            useCase(1L).test {
                val item = awaitItem()
                assertTrue(item is Result.Failure)
                assertEquals("photos error", (item as Result.Failure).error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun photo(id: Long) =
        Photo(id = id, albumId = 1L, uri = "content://photo/$id", addedAt = 0L, sortOrder = id.toInt())

    private fun fakeRepo(photosFlow: Flow<List<Photo>>) =
        object : AlbumRepository {
            override fun getAllAlbums(): Flow<List<Album>> = error("unused")
            override fun getAlbumById(albumId: Long): Flow<Album?> = error("unused")
            override suspend fun createAlbum(name: String, description: String?) = 0L
            override suspend fun updateAlbum(album: Album) = Unit
            override suspend fun deleteAlbum(album: Album) = Unit
            override suspend fun addPhotos(photos: List<Photo>) = error("unused")
            override suspend fun removePhoto(photo: Photo) = error("unused")
            override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = photosFlow
            override suspend fun persistPalette(albumId: Long, palette: BlendedPalette) = error("unused")
        }

    private fun throwingRepo() =
        object : AlbumRepository {
            override fun getAllAlbums(): Flow<List<Album>> = error("unused")
            override fun getAlbumById(albumId: Long): Flow<Album?> = error("unused")
            override suspend fun createAlbum(name: String, description: String?) = 0L
            override suspend fun updateAlbum(album: Album) = Unit
            override suspend fun deleteAlbum(album: Album) = Unit
            override suspend fun addPhotos(photos: List<Photo>) = error("unused")
            override suspend fun removePhoto(photo: Photo) = error("unused")
            override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = throw RuntimeException("photos error")
            override suspend fun persistPalette(albumId: Long, palette: BlendedPalette) = error("unused")
        }
}
