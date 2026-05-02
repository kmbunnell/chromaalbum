package com.example.chromaalbum.ui.screens

import com.example.chromaalbum.domain.FakeAlbumRepository
import com.example.chromaalbum.domain.ThrowingAlbumRepository
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.repository.AlbumRepository
import com.example.chromaalbum.domain.usecase.DeleteAlbumUseCase
import com.example.chromaalbum.domain.usecase.GetAlbumsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region — album-list state

    @Test
    fun uiState_givenNoEmissions_whenCreated_thenIsLoading() =
        runTest(testDispatcher) {
            val viewModel = homeViewModel()
            val states = mutableListOf<HomeUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }

            advanceUntilIdle()

            assertTrue(states.first().isLoading)
            assertTrue(states.first().albums.isEmpty())
            assertNull(states.first().error)
            job.cancel()
        }

    @Test
    fun uiState_givenAlbumsEmitted_whenCollected_thenAlbumsPopulatedAndNotLoading() =
        runTest(testDispatcher) {
            val viewModel =
                homeViewModel(
                    getAlbumsUseCase = GetAlbumsUseCase(FakeAlbumRepository(flowOf(listOf(album(1), album(2))))),
                )
            val states = mutableListOf<HomeUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }

            advanceUntilIdle()

            val success = states.last()
            assertFalse(success.isLoading)
            assertEquals(2, success.albums.size)
            assertNull(success.error)
            job.cancel()
        }

    @Test
    fun uiState_givenRepositoryErrors_whenCollected_thenErrorNotNullAndNotLoading() =
        runTest(testDispatcher) {
            val viewModel =
                homeViewModel(
                    getAlbumsUseCase = GetAlbumsUseCase(ThrowingAlbumRepository()),
                )
            val states = mutableListOf<HomeUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }

            advanceUntilIdle()

            val error = states.last()
            assertNotNull(error.error)
            assertFalse(error.isLoading)
            job.cancel()
        }

    // endregion

    // region — selection mode

    @Test
    fun toggleSelection_givenNoSelection_whenIdToggled_thenSelectionModeEnteredAndIdSelected() =
        runTest(testDispatcher) {
            val viewModel = homeViewModel()
            val states = mutableListOf<HomeUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            viewModel.toggleSelection(1L)
            advanceUntilIdle()

            val state = states.last()
            assertEquals(setOf(1L), state.selectedIds)
            assertTrue(state.isSelectionMode)
            job.cancel()
        }

    @Test
    fun toggleSelection_givenIdAlreadySelected_whenSameIdToggled_thenIdDeselectedAndSelectionModeExited() =
        runTest(testDispatcher) {
            val viewModel = homeViewModel()
            val states = mutableListOf<HomeUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            viewModel.toggleSelection(1L)
            viewModel.toggleSelection(1L)
            advanceUntilIdle()

            val state = states.last()
            assertTrue(state.selectedIds.isEmpty())
            assertFalse(state.isSelectionMode)
            job.cancel()
        }

    @Test
    fun toggleSelection_givenOneSelected_whenDifferentIdToggled_thenBothIdsSelected() =
        runTest(testDispatcher) {
            val viewModel = homeViewModel()
            val states = mutableListOf<HomeUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            viewModel.toggleSelection(1L)
            viewModel.toggleSelection(2L)
            advanceUntilIdle()

            assertEquals(setOf(1L, 2L), states.last().selectedIds)
            job.cancel()
        }

    @Test
    fun exitSelectionMode_givenSomeSelected_whenCalled_thenSelectedIdsEmpty() =
        runTest(testDispatcher) {
            val viewModel = homeViewModel()
            val states = mutableListOf<HomeUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            viewModel.toggleSelection(1L)
            viewModel.toggleSelection(2L)
            viewModel.exitSelectionMode()
            advanceUntilIdle()

            assertTrue(states.last().selectedIds.isEmpty())
            job.cancel()
        }

    @Test
    fun deleteSelected_givenUseCaseSucceeds_whenCalled_thenSelectionClearedAndNoError() =
        runTest(testDispatcher) {
            val viewModel =
                homeViewModel(
                    getAlbumsUseCase = GetAlbumsUseCase(FakeAlbumRepository(flowOf(listOf(album(1L), album(2L))))),
                    deleteAlbumUseCase = successDeleteUseCase(),
                )
            val states = mutableListOf<HomeUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            viewModel.toggleSelection(1L)
            viewModel.toggleSelection(2L)
            viewModel.deleteSelected()
            advanceUntilIdle()

            val state = states.last()
            assertTrue(state.selectedIds.isEmpty())
            assertNull(state.error)
            job.cancel()
        }

    @Test
    fun deleteSelected_givenUseCaseFailure_whenCalled_thenSelectionClearedAndErrorSet() =
        runTest(testDispatcher) {
            val viewModel =
                homeViewModel(
                    getAlbumsUseCase = GetAlbumsUseCase(FakeAlbumRepository(flowOf(listOf(album(1L))))),
                    deleteAlbumUseCase = failureDeleteUseCase("delete failed"),
                )
            val states = mutableListOf<HomeUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            viewModel.toggleSelection(1L)
            viewModel.deleteSelected()
            advanceUntilIdle()

            val state = states.last()
            assertTrue(state.selectedIds.isEmpty())
            assertNotNull(state.error)
            job.cancel()
        }

    // endregion

    // region — helpers

    private fun homeViewModel(
        getAlbumsUseCase: GetAlbumsUseCase = GetAlbumsUseCase(FakeAlbumRepository(flowOf())),
        deleteAlbumUseCase: DeleteAlbumUseCase = successDeleteUseCase(),
    ) = HomeViewModel(getAlbumsUseCase, deleteAlbumUseCase)

    private fun successDeleteUseCase() =
        DeleteAlbumUseCase(stubRepo(onDelete = {}))

    private fun failureDeleteUseCase(error: String) =
        DeleteAlbumUseCase(stubRepo(onDelete = { throw RuntimeException(error) }))

    private fun stubRepo(
        albumFlow: Flow<Album?> = flowOf(null),
        onDelete: suspend (Album) -> Unit = {},
    ) = object : AlbumRepository {
        override fun getAllAlbums(): Flow<List<Album>> = flowOf(emptyList())
        override fun getAlbumById(albumId: Long): Flow<Album?> = albumFlow
        override suspend fun createAlbum(name: String, description: String?) = 0L
        override suspend fun updateAlbum(album: Album) = Unit
        override suspend fun deleteAlbum(album: Album) = onDelete(album)
        override suspend fun addPhotos(photos: List<Photo>) = error("unused")
        override suspend fun removePhoto(photo: Photo) = error("unused")
        override fun getPhotosForAlbum(albumId: Long): Flow<List<Photo>> = error("unused")
        override suspend fun persistPalette(albumId: Long, palette: BlendedPalette) = error("unused")
    }

    private fun album(id: Long) =
        Album(
            id = id,
            name = "Album $id",
            description = null,
            createdAt = 0L,
            updatedAt = 0L,
            coverPhotoUri = null,
        )

    // endregion
}
