package com.example.chromaalbum.domain.usecase

import com.example.chromaalbum.data.local.entity.Album
import com.example.chromaalbum.data.local.entity.Photo
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemovePhotoUseCaseTest {
    @Test
    fun invoke_givenNonCoverPhoto_thenCoverUnchanged() =
        runTest {
            val fake = FakeRepo(album(cover = "uri://cover"))
            fake.seed(photo(id = 1, uri = "uri://cover", sortOrder = 0))
            fake.seed(photo(id = 2, uri = "uri://other", sortOrder = 1))
            val useCase = RemovePhotoUseCase(fake)

            useCase(photo(id = 2, uri = "uri://other", sortOrder = 1))

            assertEquals("uri://cover", fake.getAlbumById(ALBUM_ID).first()?.coverPhotoUri)
        }

    @Test
    fun invoke_givenCoverPhoto_thenCoverUpdatedToFirstRemainingPhoto() =
        runTest {
            val fake = FakeRepo(album(cover = "uri://cover"))
            fake.seed(photo(id = 1, uri = "uri://cover", sortOrder = 0))
            fake.seed(photo(id = 2, uri = "uri://next", sortOrder = 1))
            val useCase = RemovePhotoUseCase(fake)

            useCase(photo(id = 1, uri = "uri://cover", sortOrder = 0))

            assertEquals("uri://next", fake.getAlbumById(ALBUM_ID).first()?.coverPhotoUri)
        }

    @Test
    fun invoke_givenOnlyPhoto_thenCoverSetToNull() =
        runTest {
            val fake = FakeRepo(album(cover = "uri://only"))
            fake.seed(photo(id = 1, uri = "uri://only", sortOrder = 0))
            val useCase = RemovePhotoUseCase(fake)

            useCase(photo(id = 1, uri = "uri://only", sortOrder = 0))

            assertNull(fake.getAlbumById(ALBUM_ID).first()?.coverPhotoUri)
        }

    private companion object {
        const val ALBUM_ID = 1L

        fun album(cover: String?) =
            Album(
                id = ALBUM_ID,
                name = "Test",
                description = null,
                createdAt = 0L,
                updatedAt = 0L,
                coverPhotoUri = cover,
            )

        fun photo(id: Long, uri: String, sortOrder: Int) =
            Photo(id = id, albumId = ALBUM_ID, uri = uri, addedAt = 0L, sortOrder = sortOrder)
    }

    private class FakeRepo(initial: Album) : AlbumRepository {
        private val albums = mutableMapOf(initial.id to initial)
        private val photos = mutableListOf<Photo>()

        fun seed(photo: Photo) {
            photos.add(photo)
        }

        override fun getAlbumById(albumId: Long): Flow<Album?> = flowOf(albums[albumId])

        override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> =
            flowOf(photos.filter { it.albumId == albumId }.sortedBy { it.sortOrder })

        override suspend fun removePhoto(photo: Photo) {
            photos.removeIf { it.id == photo.id }
        }

        override suspend fun updateAlbum(album: Album) {
            albums[album.id] = album
        }

        override fun getAllAlbums(): Flow<List<Album>> = flowOf(albums.values.toList())

        override suspend fun createAlbum(name: String, description: String?): Long = error("unused")

        override suspend fun deleteAlbum(album: Album): Unit = error("unused")

        override suspend fun addPhotos(photos: List<Photo>): Unit = error("unused")

        override suspend fun persistPalette(albumId: Long, palette: BlendedPalette): Unit = error("unused")
    }
}
