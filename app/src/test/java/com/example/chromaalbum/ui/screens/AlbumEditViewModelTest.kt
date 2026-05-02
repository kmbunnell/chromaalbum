package com.example.chromaalbum.ui.screens

import androidx.lifecycle.SavedStateHandle
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.repository.AlbumRepository
import com.example.chromaalbum.domain.usecase.GetAlbumUseCase
import com.example.chromaalbum.domain.usecase.GetPhotosForAlbumUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumEditViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_givenEditModeWithAlbum_whenBothFlowsEmit_thenUiStateHasNameDescriptionAndPhotos() =
        runTest(testDispatcher) {
            val albumFlow = MutableSharedFlow<Album?>(replay = 1)
            val photosFlow = MutableSharedFlow<List<Photo>>(replay = 1)
            albumFlow.emit(album(1L, "Vacation", "Summer 2024"))
            photosFlow.emit(listOf(photo(1), photo(2), photo(3)))

            val viewModel = viewModel(albumId = 1L, albumFlow = albumFlow, photosFlow = photosFlow)
            val states = mutableListOf<AlbumEditUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            val state = states.last()
            assertEquals("Vacation", state.name)
            assertEquals("Summer 2024", state.description)
            assertEquals(3, state.photos.size)
            assertFalse(state.isCreateMode)
            job.cancel()
        }

    @Test
    fun init_givenEditModeWithEmptyDescription_whenLoaded_thenUiStateDescriptionIsEmpty() =
        runTest(testDispatcher) {
            val albumFlow = MutableSharedFlow<Album?>(replay = 1)
            val photosFlow = MutableSharedFlow<List<Photo>>(replay = 1)
            albumFlow.emit(album(1L, "Beach", null))
            photosFlow.emit(emptyList())

            val viewModel = viewModel(albumId = 1L, albumFlow = albumFlow, photosFlow = photosFlow)
            val states = mutableListOf<AlbumEditUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            assertEquals("", states.last().description)
            job.cancel()
        }

    @Test
    fun init_givenAlbumWithNoPhotos_whenLoaded_thenUiStateHasEmptyPhotoList() =
        runTest(testDispatcher) {
            val albumFlow = MutableSharedFlow<Album?>(replay = 1)
            val photosFlow = MutableSharedFlow<List<Photo>>(replay = 1)
            albumFlow.emit(album(1L, "Empty", "No photos yet"))
            photosFlow.emit(emptyList())

            val viewModel = viewModel(albumId = 1L, albumFlow = albumFlow, photosFlow = photosFlow)
            val states = mutableListOf<AlbumEditUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            val state = states.last()
            assertTrue(state.photos.isEmpty())
            assertEquals("Empty", state.name)
            job.cancel()
        }

    @Test
    fun init_givenAlbumLoadFailure_whenInit_thenUiStateHasError() =
        runTest(testDispatcher) {
            val albumFlow = MutableSharedFlow<Album?>(replay = 1)
            val photosFlow = MutableSharedFlow<List<Photo>>(replay = 1)
            // null → GetAlbumUseCase maps to Failure
            albumFlow.emit(null)
            photosFlow.emit(emptyList())

            val viewModel = viewModel(albumId = 99L, albumFlow = albumFlow, photosFlow = photosFlow)
            val states = mutableListOf<AlbumEditUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            assertNotNull(states.last().error)
            job.cancel()
        }

    // region — helpers

    private fun viewModel(
        albumId: Long?,
        albumFlow: Flow<Album?>,
        photosFlow: Flow<List<Photo>>,
    ): AlbumEditViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("albumId" to albumId))
        val repo = stubRepo(albumFlow = albumFlow, photosFlow = photosFlow)
        return AlbumEditViewModel(
            savedStateHandle = savedStateHandle,
            getAlbumUseCase = GetAlbumUseCase(repo),
            getPhotosForAlbumUseCase = GetPhotosForAlbumUseCase(repo),
        )
    }

    private fun stubRepo(
        albumFlow: Flow<Album?>,
        photosFlow: Flow<List<Photo>>,
    ) = object : AlbumRepository {
        override fun getAllAlbums(): Flow<List<Album>> = error("unused")
        override fun getAlbumById(albumId: Long): Flow<Album?> = albumFlow
        override suspend fun createAlbum(name: String, description: String?) = 0L
        override suspend fun updateAlbum(album: Album) = Unit
        override suspend fun deleteAlbum(album: Album) = Unit
        override suspend fun addPhotos(photos: List<Photo>) = error("unused")
        override suspend fun removePhoto(photo: Photo) = error("unused")
        override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = photosFlow
        override suspend fun persistPalette(albumId: Long, palette: BlendedPalette) = error("unused")
    }

    private fun album(id: Long, name: String, description: String?) =
        Album(id = id, name = name, description = description, createdAt = 0L, updatedAt = 0L, coverPhotoUri = null)

    private fun photo(id: Long) =
        Photo(id = id, albumId = 1L, uri = "content://photo/$id", addedAt = 0L, sortOrder = id.toInt())

    // endregion
}
