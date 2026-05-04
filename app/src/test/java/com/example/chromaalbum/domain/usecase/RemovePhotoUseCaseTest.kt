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

class RemovePhotoUseCaseTest {
    @Test
    fun invoke_givenPhoto_whenInvoked_thenPhotoRemovedFromRepo() =
        runTest {
            val fake = FakeRepo(album())
            val p = photo(id = 1, uri = "uri://a", sortOrder = 0)
            fake.seed(p)
            val useCase = RemovePhotoUseCase(fake)

            useCase(p)

            assertEquals(emptyList<Photo>(), fake.getPhotosForAlbum(ALBUM_ID).first())
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

        override suspend fun persistPalette(albumId: Long, palette: com.example.chromaalbum.domain.model.BlendedPalette): Unit = error("unused")
    }
}
