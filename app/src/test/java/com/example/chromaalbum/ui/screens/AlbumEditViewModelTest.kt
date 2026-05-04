package com.example.chromaalbum.ui.screens

import androidx.lifecycle.SavedStateHandle
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.repository.AlbumRepository
import com.example.chromaalbum.domain.usecase.AddPhotosUseCase
import com.example.chromaalbum.domain.usecase.CreateAlbumUseCase
import com.example.chromaalbum.domain.usecase.GetAlbumUseCase
import com.example.chromaalbum.domain.usecase.GetPhotosForAlbumUseCase
import com.example.chromaalbum.domain.usecase.SaveEditedAlbumUseCase
import com.example.chromaalbum.domain.usecase.SaveNewAlbumUseCase
import com.example.chromaalbum.domain.usecase.UpdateAlbumUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
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
import org.junit.Assert.assertNull
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

    @Test
    fun onPhotosSelected_givenUris_whenCalled_thenDisplayUrisAppended() =
        runTest(testDispatcher) {
            val viewModel = viewModel(albumId = null)
            val states = mutableListOf<AlbumEditUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }

            viewModel.onPhotosSelected(listOf("content://photo/1", "content://photo/2"))
            advanceUntilIdle()

            assertEquals(listOf("content://photo/1", "content://photo/2"), states.last().displayUris)
            job.cancel()
        }

    @Test
    fun onDone_givenUseCaseSuccess_whenDone_thenNavigateUpTrueAndIsSavingTrue() =
        runTest(testDispatcher) {
            val viewModel = viewModel(albumId = null, saveResult = Result.Success(1L))
            val states = mutableListOf<AlbumEditUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }

            viewModel.onDone()
            advanceUntilIdle()

            val last = states.last()
            assertTrue(last.navigateUp)
            assertTrue(last.isSaving)
            job.cancel()
        }

    @Test
    fun onDone_givenUseCaseFailure_whenDone_thenErrorSetAndNoNavigation() =
        runTest(testDispatcher) {
            val viewModel = viewModel(albumId = null, saveResult = Result.Failure("save failed"))
            val states = mutableListOf<AlbumEditUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }

            viewModel.onDone()
            advanceUntilIdle()

            val last = states.last()
            assertEquals("save failed", last.error)
            assertFalse(last.navigateUp)
            assertFalse(last.isSaving)
            job.cancel()
        }

    @Test
    fun onNavigatedUp_givenNavigateUpTrue_whenCalled_thenNavigateUpFalse() =
        runTest(testDispatcher) {
            val viewModel = viewModel(albumId = null, saveResult = Result.Success(1L))
            val states = mutableListOf<AlbumEditUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }

            viewModel.onDone()
            advanceUntilIdle()
            assertTrue(states.last().navigateUp)

            viewModel.onNavigatedUp()
            advanceUntilIdle()

            assertFalse(states.last().navigateUp)
            assertFalse(states.last().isSaving)
            assertNull(states.last().error)
            job.cancel()
        }

    @Test
    fun onPhotosSelected_givenEditMode_whenCalled_thenDisplayUrisContainExistingAndNewPhotos() =
        runTest(testDispatcher) {
            val albumFlow = MutableSharedFlow<Album?>(replay = 1)
            val photosFlow = MutableSharedFlow<List<Photo>>(replay = 1)
            albumFlow.emit(album(1L, "Vacation", null))
            photosFlow.emit(listOf(photo(1), photo(2)))

            val viewModel = viewModel(albumId = 1L, albumFlow = albumFlow, photosFlow = photosFlow)
            val states = mutableListOf<AlbumEditUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            viewModel.onPhotosSelected(listOf("content://photo/new"))
            advanceUntilIdle()

            val displayUris = states.last().displayUris
            assertTrue(displayUris.contains("content://photo/1"))
            assertTrue(displayUris.contains("content://photo/2"))
            assertTrue(displayUris.contains("content://photo/new"))
            job.cancel()
        }

    @Test
    fun onDone_givenEditModeAndSaveEditedSucceeds_whenDone_thenNavigateUpTrueAndIsSavingTrue() =
        runTest(testDispatcher) {
            val albumFlow = MutableSharedFlow<Album?>(replay = 1)
            val photosFlow = MutableSharedFlow<List<Photo>>(replay = 1)
            albumFlow.emit(album(1L, "Name", null))
            photosFlow.emit(emptyList())

            val viewModel =
                viewModel(
                    albumId = 1L,
                    albumFlow = albumFlow,
                    photosFlow = photosFlow,
                    saveEditedResult = Result.Success(Unit),
                )
            val states = mutableListOf<AlbumEditUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            viewModel.onDone()
            advanceUntilIdle()

            val last = states.last()
            assertTrue(last.navigateUp)
            assertTrue(last.isSaving)
            job.cancel()
        }

    @Test
    fun onDone_givenEditModeAndSaveEditedFails_whenDone_thenErrorSetAndNoNavigation() =
        runTest(testDispatcher) {
            val albumFlow = MutableSharedFlow<Album?>(replay = 1)
            val photosFlow = MutableSharedFlow<List<Photo>>(replay = 1)
            albumFlow.emit(album(1L, "Name", null))
            photosFlow.emit(emptyList())

            val viewModel =
                viewModel(
                    albumId = 1L,
                    albumFlow = albumFlow,
                    photosFlow = photosFlow,
                    saveEditedResult = Result.Failure("edit failed"),
                )
            val states = mutableListOf<AlbumEditUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            viewModel.onDone()
            advanceUntilIdle()

            val last = states.last()
            assertNotNull(last.error)
            assertFalse(last.navigateUp)
            assertFalse(last.isSaving)
            job.cancel()
        }

    // region — helpers

    /**
     * Builds a SaveNewAlbumUseCase whose outcome is driven entirely by [saveResult].
     *
     * The VM tests call onDone() with no photos selected, so AddPhotosUseCase is never reached.
     * For failures we throw inside createAlbum so CreateAlbumUseCase's own .catch produces
     * the Failure with the expected message without needing a mock framework.
     */
    private fun fakeSave(saveResult: Result<Long, String>): SaveNewAlbumUseCase {
        val controlRepo =
            object : AlbumRepository {
                override suspend fun createAlbum(name: String, description: String?): Long =
                    when (saveResult) {
                        is Result.Success -> saveResult.data
                        is Result.Failure -> throw RuntimeException(saveResult.error)
                    }

                override fun getAllAlbums(): Flow<List<Album>> = error("unused")
                override fun getAlbumById(albumId: Long): Flow<Album?> = error("unused")
                override suspend fun updateAlbum(album: Album) = Unit
                override suspend fun deleteAlbum(album: Album): Unit = error("unused")
                override suspend fun addPhotos(photos: List<Photo>): Unit = error("unused")
                override suspend fun removePhoto(photo: Photo): Unit = error("unused")
                override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = error("unused")
                override suspend fun persistPalette(albumId: Long, palette: BlendedPalette): Unit = error("unused")
            }
        return SaveNewAlbumUseCase(
            createAlbumUseCase = CreateAlbumUseCase(controlRepo),
            addPhotosUseCase = AddPhotosUseCase(controlRepo),
            blendFn = { null },
            repository = controlRepo,
        )
    }

    private fun viewModel(
        albumId: Long?,
        albumFlow: Flow<Album?> = flowOf(null),
        photosFlow: Flow<List<Photo>> = flowOf(emptyList()),
        saveResult: Result<Long, String> = Result.Success(0L),
        saveEditedResult: Result<Unit, String> = Result.Success(Unit),
    ): AlbumEditViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("albumId" to albumId))
        val repo = stubRepo(albumFlow = albumFlow, photosFlow = photosFlow)
        return AlbumEditViewModel(
            savedStateHandle = savedStateHandle,
            getAlbumUseCase = GetAlbumUseCase(repo),
            getPhotosForAlbumUseCase = GetPhotosForAlbumUseCase(repo),
            saveNewAlbumUseCase = fakeSave(saveResult),
            saveEditedAlbumUseCase = fakeEditSave(saveEditedResult),
        )
    }

    private fun fakeEditSave(saveResult: Result<Unit, String>): SaveEditedAlbumUseCase {
        val controlRepo =
            object : AlbumRepository {
                override fun getAlbumById(albumId: Long): Flow<Album?> =
                    when (saveResult) {
                        is Result.Success -> flowOf(album(albumId, "Test", null))
                        is Result.Failure -> flowOf(null)
                    }

                override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = flowOf(emptyList())

                override suspend fun updateAlbum(album: Album) = Unit

                override suspend fun addPhotos(photos: List<Photo>) = Unit

                override suspend fun persistPalette(albumId: Long, palette: BlendedPalette) = Unit

                override fun getAllAlbums(): Flow<List<Album>> = error("unused")

                override suspend fun createAlbum(name: String, description: String?): Long = error("unused")

                override suspend fun deleteAlbum(album: Album): Unit = error("unused")

                override suspend fun removePhoto(photo: Photo): Unit = error("unused")
            }
        return SaveEditedAlbumUseCase(
            updateAlbumUseCase = UpdateAlbumUseCase(controlRepo),
            addPhotosUseCase = AddPhotosUseCase(controlRepo),
            blendFn = { null },
            repository = controlRepo,
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
        Album(id = id, name = name, description = description, createdAt = 0L, updatedAt = 0L)

    private fun photo(id: Long) =
        Photo(id = id, albumId = 1L, uri = "content://photo/$id", addedAt = 0L, sortOrder = id.toInt())

    // endregion
}
