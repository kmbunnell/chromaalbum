package com.example.chromaalbum.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.chromaalbum.data.helper.UriNotPersistableException
import com.example.chromaalbum.data.helper.UriPersistenceHelper
import com.example.chromaalbum.data.local.ChromaAlbumDatabase
import com.example.chromaalbum.data.local.entity.Album
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.SwatchData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private class FakeUriPersistenceHelper(
    private val throwOnCallIndex: Int = -1,
) : UriPersistenceHelper {
    val capturedUris = mutableListOf<Uri>()
    val releasedUris = mutableListOf<Uri>()
    private var callCount = 0

    override fun persist(uri: Uri) {
        val index = callCount++
        capturedUris += uri
        if (index == throwOnCallIndex) throw UriNotPersistableException(uri, SecurityException("fake"))
    }

    override fun release(uri: Uri) {
        releasedUris += uri
    }
}

// sdk = [35]: robolectric 4.14.1 caps at API 35 — bump when robolectric supports higher
@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)
class AlbumRepositoryImplTest {
    private lateinit var db: ChromaAlbumDatabase
    private lateinit var fakeHelper: FakeUriPersistenceHelper
    private lateinit var repository: AlbumRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, ChromaAlbumDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        fakeHelper = FakeUriPersistenceHelper()
        repository = AlbumRepositoryImpl(db.albumDao(), db.photoDao(), db, fakeHelper, UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createAlbum_givenValidInput_whenCalled_thenSetsTimestampsDefaultPaletteAndReturnsId() =
        runTest {
            // When
            val id = repository.createAlbum("My Album", "desc")

            // Then
            assertTrue(id > 0)
            val album = repository.getAlbumById(id).first()!!
            assertTrue(album.createdAt != 0L)
            assertTrue(album.updatedAt != 0L)
            assertEquals(Album.EMPTY_PALETTE_JSON, album.paletteJson)
        }

    @Test
    fun addPhotos_givenEmptyUriList_whenCalled_thenNoOpAndCoverUnchanged() =
        runTest {
            // Given
            val albumId = repository.createAlbum("Album", null)

            // When
            repository.addPhotos(albumId, emptyList())

            // Then
            assertTrue(repository.getPhotosForAlbum(albumId).first().isEmpty())
            assertNull(repository.getAlbumById(albumId).first()!!.coverPhotoUri)
        }

    @Test
    fun addPhotos_givenUriList_whenCalled_thenPhotosStoredWithIncrementingSortOrder() =
        runTest {
            // Given
            val albumId = repository.createAlbum("Album", null)

            // When
            repository.addPhotos(albumId, listOf("uri://1", "uri://2", "uri://3"))

            // Then
            val photos = repository.getPhotosForAlbum(albumId).first()
            assertEquals(3, photos.size)
            assertEquals(0, photos[0].sortOrder)
            assertEquals(1, photos[1].sortOrder)
            assertEquals(2, photos[2].sortOrder)
        }

    @Test
    fun addPhotos_givenSecondBatch_whenCalled_thenSortOrderContinuesFromPrevious() =
        runTest {
            // Given
            val albumId = repository.createAlbum("Album", null)
            repository.addPhotos(albumId, listOf("uri://1", "uri://2"))

            // When
            repository.addPhotos(albumId, listOf("uri://3", "uri://4"))

            // Then
            val photos = repository.getPhotosForAlbum(albumId).first()
            assertEquals(4, photos.size)
            assertEquals(listOf(0, 1, 2, 3), photos.map { it.sortOrder })
        }

    @Test
    fun addPhotos_givenNullCover_whenCalled_thenCoverSetToFirstUri() =
        runTest {
            // Given
            val albumId = repository.createAlbum("Album", null)

            // When
            repository.addPhotos(albumId, listOf("uri://first", "uri://second"))

            // Then
            val album = repository.getAlbumById(albumId).first()!!
            assertEquals("uri://first", album.coverPhotoUri)
        }

    @Test
    fun addPhotos_givenExistingCover_whenCalled_thenCoverUnchanged() =
        runTest {
            // Given
            val albumId = repository.createAlbum("Album", null)
            repository.addPhotos(albumId, listOf("uri://first"))

            // When
            repository.addPhotos(albumId, listOf("uri://second", "uri://third"))

            // Then
            val album = repository.getAlbumById(albumId).first()!!
            assertEquals("uri://first", album.coverPhotoUri)
        }

    @Test
    fun removePhoto_givenNonCoverPhoto_whenCalled_thenPhotoDeletedAndCoverUnchanged() =
        runTest {
            // Given
            val albumId = repository.createAlbum("Album", null)
            repository.addPhotos(albumId, listOf("uri://cover", "uri://other"))
            val photos = repository.getPhotosForAlbum(albumId).first()
            val nonCoverPhoto = photos.first { it.uri == "uri://other" }

            // When
            repository.removePhoto(nonCoverPhoto)

            // Then
            val remaining = repository.getPhotosForAlbum(albumId).first()
            assertTrue(remaining.none { it.id == nonCoverPhoto.id })
            val album = repository.getAlbumById(albumId).first()!!
            assertEquals("uri://cover", album.coverPhotoUri)
        }

    @Test
    fun removePhoto_givenCoverPhoto_whenCalled_thenCoverUpdatedToNextPhoto() =
        runTest {
            // Given
            val albumId = repository.createAlbum("Album", null)
            repository.addPhotos(albumId, listOf("uri://cover", "uri://next"))
            val coverPhoto = repository.getPhotosForAlbum(albumId).first().first { it.uri == "uri://cover" }

            // When
            repository.removePhoto(coverPhoto)

            // Then
            val album = repository.getAlbumById(albumId).first()!!
            assertEquals("uri://next", album.coverPhotoUri)
        }

    @Test
    fun removePhoto_givenOnlyPhoto_whenCalled_thenCoverSetToNull() =
        runTest {
            // Given
            val albumId = repository.createAlbum("Album", null)
            repository.addPhotos(albumId, listOf("uri://only"))
            val photo = repository.getPhotosForAlbum(albumId).first().first()

            // When
            repository.removePhoto(photo)

            // Then
            val album = repository.getAlbumById(albumId).first()!!
            assertNull(album.coverPhotoUri)
        }

    @Test
    fun persistPalette_givenPaletteWithVibrant_whenCalled_thenDominantColorIsVibrantHex() =
        runTest {
            // Given
            val albumId = repository.createAlbum("Album", null)
            val palette =
                BlendedPalette(
                    vibrant = SwatchData(hex = "#FF0000", titleText = "#FFFFFF"),
                    darkVibrant = SwatchData(hex = "#AA0000", titleText = "#FFFFFF"),
                    lightVibrant = null,
                    muted = null,
                    darkMuted = null,
                    lightMuted = null,
                )

            // When
            repository.persistPalette(albumId, palette)

            // Then
            val album = repository.getAlbumById(albumId).first()!!
            assertEquals(palette.vibrant!!.hex, album.dominantColor)
        }

    @Test
    fun persistPalette_givenPaletteWithNullVibrant_whenCalled_thenDominantColorIsFirstNonNullSwatchHex() =
        runTest {
            // Given
            val albumId = repository.createAlbum("Album", null)
            val palette =
                BlendedPalette(
                    vibrant = null,
                    darkVibrant = SwatchData(hex = "#AA0000", titleText = "#FFFFFF"),
                    lightVibrant = null,
                    muted = null,
                    darkMuted = null,
                    lightMuted = null,
                )

            // When
            repository.persistPalette(albumId, palette)

            // Then
            val album = repository.getAlbumById(albumId).first()!!
            assertEquals(palette.darkVibrant!!.hex, album.dominantColor)
        }

    @Test
    fun persistPalette_givenAllNullSwatches_whenCalled_thenDominantColorIsNull() =
        runTest {
            // Given
            val albumId = repository.createAlbum("Album", null)
            val palette =
                BlendedPalette(
                    vibrant = null,
                    darkVibrant = null,
                    lightVibrant = null,
                    muted = null,
                    darkMuted = null,
                    lightMuted = null,
                )

            // When
            repository.persistPalette(albumId, palette)

            // Then
            val album = repository.getAlbumById(albumId).first()!!
            assertNull(album.dominantColor)
        }

    @Test
    fun persistPalette_givenPalette_whenCalled_thenPaletteJsonRoundTripsFromDb() =
        runTest {
            // Given
            val albumId = repository.createAlbum("Album", null)
            val palette =
                BlendedPalette(
                    vibrant = SwatchData(hex = "#FF0000", titleText = "#FFFFFF"),
                    darkVibrant = null,
                    lightVibrant = SwatchData(hex = "#FF6666", titleText = "#000000"),
                    muted = null,
                    darkMuted = null,
                    lightMuted = null,
                )

            // When
            repository.persistPalette(albumId, palette)

            // Then
            val album = repository.getAlbumById(albumId).first()!!
            assertEquals(palette, Json.decodeFromString<BlendedPalette>(album.paletteJson))
        }

    @Test
    fun addPhotos_givenUris_whenCalled_thenHelperPersistCalledForEachUri() =
        runTest {
            // Given
            val albumId = repository.createAlbum("Album", null)
            val uris = listOf("content://com.example/1", "content://com.example/2")

            // When
            repository.addPhotos(albumId, uris)

            // Then
            assertEquals(uris.map { Uri.parse(it) }, fakeHelper.capturedUris)
        }

    @Test
    fun addPhotos_givenAllPersistSucceedButTransactionFails_whenCalled_thenAllPersistedUrisReleased() =
        runTest {
            // Given
            val albumId = repository.createAlbum("Album", null)
            val uris = listOf("content://com.example/1", "content://com.example/2")
            val releasedUris = mutableListOf<Uri>()
            val dbClosingHelper =
                object : UriPersistenceHelper {
                    private val persisted = mutableListOf<Uri>()

                    override fun persist(uri: Uri) {
                        persisted += uri
                        if (persisted.size == uris.size) db.close()
                    }

                    override fun release(uri: Uri) {
                        releasedUris += uri
                    }
                }
            val testRepo = AlbumRepositoryImpl(db.albumDao(), db.photoDao(), db, dbClosingHelper, UnconfinedTestDispatcher())

            // When
            var caught: Exception? = null
            try {
                testRepo.addPhotos(albumId, uris)
            } catch (e: Exception) {
                caught = e
            }

            // Then
            assertNotNull(caught)
            assertEquals(uris.map { Uri.parse(it) }, releasedUris)
        }

    @Test
    fun addPhotos_givenPartialBatchPersistFails_whenSecondUriThrows_thenAlreadyPersistedUrisReleasedAndNoPhotosInserted() =
        runTest {
            // Given
            val albumId = repository.createAlbum("Album", null)
            fakeHelper = FakeUriPersistenceHelper(throwOnCallIndex = 1)
            repository = AlbumRepositoryImpl(db.albumDao(), db.photoDao(), db, fakeHelper, UnconfinedTestDispatcher())
            val uris = listOf("content://com.example/1", "content://com.example/2")

            // When
            var caught: UriNotPersistableException? = null
            try {
                repository.addPhotos(albumId, uris)
            } catch (e: UriNotPersistableException) {
                caught = e
            }

            // Then
            assertNotNull(caught)
            assertEquals(listOf(Uri.parse("content://com.example/1")), fakeHelper.releasedUris)
            assertTrue(repository.getPhotosForAlbum(albumId).first().isEmpty())
        }
}
