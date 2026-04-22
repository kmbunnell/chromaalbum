package com.example.chromaalbum.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.chromaalbum.data.helper.UriNotPersistableException
import com.example.chromaalbum.data.helper.UriPersistenceHelper
import com.example.chromaalbum.data.local.ChromaAlbumDatabase
import com.example.chromaalbum.data.local.entity.Album
import com.example.chromaalbum.data.local.entity.Photo
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
        repository = AlbumRepositoryImpl(db.albumDao(), db.photoDao(), fakeHelper, UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createAlbum_givenValidInput_whenCalled_thenSetsTimestampsDefaultPaletteAndReturnsId() =
        runTest {
            val id = repository.createAlbum("My Album", "desc")

            assertTrue(id > 0)
            val album = repository.getAlbumById(id).first()!!
            assertTrue(album.createdAt != 0L)
            assertTrue(album.updatedAt != 0L)
            assertEquals(Album.EMPTY_PALETTE_JSON, album.paletteJson)
        }

    @Test
    fun addPhotos_givenUris_whenCalled_thenHelperPersistCalledForEachUri() =
        runTest {
            val albumId = repository.createAlbum("Album", null)
            val uris = listOf("content://com.example/1", "content://com.example/2")

            repository.addPhotos(photos(albumId, uris))

            assertEquals(uris.map { Uri.parse(it) }, fakeHelper.capturedUris)
        }

    @Test
    fun addPhotos_givenAllPersistSucceedButTransactionFails_whenCalled_thenAllPersistedUrisReleased() =
        runTest {
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
            val testRepo = AlbumRepositoryImpl(db.albumDao(), db.photoDao(), dbClosingHelper, UnconfinedTestDispatcher())

            var caught: Exception? = null
            try {
                testRepo.addPhotos(photos(albumId, uris))
            } catch (e: Exception) {
                caught = e
            }

            assertNotNull(caught)
            assertEquals(uris.map { Uri.parse(it) }, releasedUris)
        }

    @Test
    fun addPhotos_givenPartialBatchPersistFails_whenSecondUriThrows_thenAlreadyPersistedUrisReleasedAndNoPhotosInserted() =
        runTest {
            val albumId = repository.createAlbum("Album", null)
            fakeHelper = FakeUriPersistenceHelper(throwOnCallIndex = 1)
            repository = AlbumRepositoryImpl(db.albumDao(), db.photoDao(), fakeHelper, UnconfinedTestDispatcher())
            val uris = listOf("content://com.example/1", "content://com.example/2")

            var caught: UriNotPersistableException? = null
            try {
                repository.addPhotos(photos(albumId, uris))
            } catch (e: UriNotPersistableException) {
                caught = e
            }

            assertNotNull(caught)
            assertEquals(listOf(Uri.parse("content://com.example/1")), fakeHelper.releasedUris)
            assertTrue(repository.getPhotosForAlbum(albumId).first().isEmpty())
        }

    @Test
    fun persistPalette_givenPaletteWithVibrant_whenCalled_thenDominantColorIsVibrantHex() =
        runTest {
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

            repository.persistPalette(albumId, palette)

            val album = repository.getAlbumById(albumId).first()!!
            assertEquals(palette.vibrant!!.hex, album.dominantColor)
        }

    @Test
    fun persistPalette_givenPaletteWithNullVibrant_whenCalled_thenDominantColorIsFirstNonNullSwatchHex() =
        runTest {
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

            repository.persistPalette(albumId, palette)

            val album = repository.getAlbumById(albumId).first()!!
            assertEquals(palette.darkVibrant!!.hex, album.dominantColor)
        }

    @Test
    fun persistPalette_givenAllNullSwatches_whenCalled_thenDominantColorIsNull() =
        runTest {
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

            repository.persistPalette(albumId, palette)

            val album = repository.getAlbumById(albumId).first()!!
            assertNull(album.dominantColor)
        }

    @Test
    fun persistPalette_givenPalette_whenCalled_thenPaletteJsonRoundTripsFromDb() =
        runTest {
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

            repository.persistPalette(albumId, palette)

            val album = repository.getAlbumById(albumId).first()!!
            assertEquals(palette, Json.decodeFromString<BlendedPalette>(album.paletteJson))
        }

    private fun photos(albumId: Long, uris: List<String>): List<Photo> =
        uris.mapIndexed { i, uri -> Photo(albumId = albumId, uri = uri, addedAt = 0L, sortOrder = i) }
}
