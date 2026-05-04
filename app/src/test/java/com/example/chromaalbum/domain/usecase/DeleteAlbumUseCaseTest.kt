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
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteAlbumUseCaseTest {

    private val testAlbum =
        Album(
            id = 1L,
            name = "Test Album",
            description = null,
            createdAt = 0L,
            updatedAt = 0L,
        )

    @Test
    fun invoke_givenRepoSucceeds_whenCalled_thenEmitsSuccess() =
        runTest {
            val useCase = DeleteAlbumUseCase(stubRepo(onDelete = {}))

            useCase(testAlbum).test {
                assertTrue(awaitItem() is Result.Success)
                awaitComplete()
            }
        }

    @Test
    fun invoke_givenRepoThrows_whenCalled_thenEmitsFailure() =
        runTest {
            val useCase = DeleteAlbumUseCase(stubRepo(onDelete = { throw RuntimeException("err") }))

            useCase(testAlbum).test {
                val item = awaitItem()
                assertTrue(item is Result.Failure)
                assertEquals("err", (item as Result.Failure).error)
                awaitComplete()
            }
        }

    private fun stubRepo(onDelete: suspend (Album) -> Unit) =
        object : AlbumRepository {
            override fun getAllAlbums(): Flow<List<Album>> = error("unused")
            override fun getAlbumById(albumId: Long): Flow<Album?> = error("unused")
            override suspend fun createAlbum(name: String, description: String?): Long = error("unused")
            override suspend fun updateAlbum(album: Album) = error("unused")
            override suspend fun deleteAlbum(album: Album) = onDelete(album)
            override suspend fun addPhotos(photos: List<Photo>) = error("unused")
            override suspend fun removePhoto(photo: Photo) = error("unused")
            override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = error("unused")
            override suspend fun persistPalette(albumId: Long, palette: BlendedPalette) = error("unused")
        }
}
