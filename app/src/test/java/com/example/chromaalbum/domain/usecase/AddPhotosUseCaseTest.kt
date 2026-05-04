package com.example.chromaalbum.domain.usecase

import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AddPhotosUseCaseTest {
    @Test
    fun invoke_givenEmptyUriList_thenNoPhotosAdded() =
        runTest {
            val fake = FakeRepo(album())
            val useCase = AddPhotosUseCase(fake)

            useCase(ALBUM_ID, emptyList())

            assertEquals(emptyList<Photo>(), fake.getPhotosForAlbum(ALBUM_ID).first())
        }

    @Test
    fun invoke_givenUriList_thenSortOrderStartsFromZero() =
        runTest {
            val fake = FakeRepo(album())
            val useCase = AddPhotosUseCase(fake)

            useCase(ALBUM_ID, listOf("uri://1", "uri://2", "uri://3"))

            val orders = fake.getPhotosForAlbum(ALBUM_ID).first().map { it.sortOrder }
            assertEquals(listOf(0, 1, 2), orders)
        }

    @Test
    fun invoke_givenSecondBatch_thenSortOrderContinuesFromPreviousBatch() =
        runTest {
            val fake = FakeRepo(album())
            val useCase = AddPhotosUseCase(fake)
            useCase(ALBUM_ID, listOf("uri://1", "uri://2"))

            useCase(ALBUM_ID, listOf("uri://3", "uri://4"))

            val orders = fake.getPhotosForAlbum(ALBUM_ID).first().map { it.sortOrder }
            assertEquals(listOf(0, 1, 2, 3), orders)
        }

    private companion object {
        const val ALBUM_ID = 1L

        fun album() =
            Album(
                id = ALBUM_ID,
                name = "Test",
                description = null,
                createdAt = 0L,
                updatedAt = 0L,
            )
    }

    private class FakeRepo(initial: Album) : AlbumRepository {
        private val albums = mutableMapOf(initial.id to initial)
        private val photos = mutableListOf<Photo>()
        private var nextId = 1L

        override fun getAlbumById(albumId: Long): Flow<Album?> = flowOf(albums[albumId])

        override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> =
            flowOf(photos.filter { it.albumId == albumId }.sortedBy { it.sortOrder })

        override suspend fun addPhotos(photoList: List<Photo>) {
            photoList.forEach { photos.add(it.copy(id = nextId++)) }
        }

        override suspend fun updateAlbum(album: Album) {
            albums[album.id] = album
        }

        override fun getAllAlbums(): Flow<List<Album>> = flowOf(albums.values.toList())

        override suspend fun createAlbum(name: String, description: String?): Long = error("unused")

        override suspend fun deleteAlbum(album: Album): Unit = error("unused")

        override suspend fun removePhoto(photo: Photo): Unit = error("unused")

        override suspend fun persistPalette(albumId: Long, palette: com.example.chromaalbum.domain.model.BlendedPalette): Unit = error("unused")
    }
}
