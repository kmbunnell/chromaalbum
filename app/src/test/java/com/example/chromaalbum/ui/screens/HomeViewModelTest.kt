package com.example.chromaalbum.ui.screens

import app.cash.turbine.test
import com.example.chromaalbum.domain.FakeAlbumRepository
import com.example.chromaalbum.domain.ThrowingAlbumRepository
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.ui.components.AlbumSheetMode
import com.example.chromaalbum.domain.model.BlendedPalette
import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.repository.AlbumRepository
import com.example.chromaalbum.domain.usecase.CreateAlbumUseCase
import com.example.chromaalbum.domain.usecase.GetAlbumsUseCase
import com.example.chromaalbum.domain.usecase.UpdateAlbumUseCase
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

    // region — existing album-list state tests

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

    // region — sheet mode and write actions

    @Test
    fun showCreateSheet_whenCalled_thenSheetModeIsCreate() =
        runTest(testDispatcher) {
            val viewModel = homeViewModel()
            val states = mutableListOf<HomeUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            viewModel.showCreateSheet()
            advanceUntilIdle()

            assertEquals(AlbumSheetMode.Create, states.last().sheetMode)
            job.cancel()
        }

    @Test
    fun showEditSheet_givenAlbum_whenCalled_thenSheetModeIsEditWithAlbum() =
        runTest(testDispatcher) {
            val targetAlbum = album(7L)
            val viewModel = homeViewModel()
            val states = mutableListOf<HomeUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            viewModel.showEditSheet(targetAlbum)
            advanceUntilIdle()

            assertEquals(AlbumSheetMode.Edit(targetAlbum), states.last().sheetMode)
            job.cancel()
        }

    @Test
    fun dismissSheet_whenCalled_thenSheetModeIsHidden() =
        runTest(testDispatcher) {
            val viewModel = homeViewModel()
            val states = mutableListOf<HomeUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            viewModel.showCreateSheet()
            advanceUntilIdle()
            viewModel.dismissSheet()
            advanceUntilIdle()

            assertEquals(AlbumSheetMode.Hidden, states.last().sheetMode)
            job.cancel()
        }

    @Test
    fun createAlbum_givenSuccess_whenCompletes_thenSheetHiddenAndNavigationEventEmitted() =
        runTest(testDispatcher) {
            val viewModel = homeViewModel(createAlbumUseCase = successCreateUseCase(99L))
            val states = mutableListOf<HomeUiState>()
            val navEvents = mutableListOf<Long>()
            val stateJob = launch { viewModel.uiState.collect { states.add(it) } }
            val navJob = launch { viewModel.navigationEvents.collect { navEvents.add(it) } }
            advanceUntilIdle()

            viewModel.showCreateSheet()
            advanceUntilIdle()
            viewModel.createAlbum("New Album", null)
            advanceUntilIdle()

            assertEquals(AlbumSheetMode.Hidden, states.last().sheetMode)
            assertEquals(listOf(99L), navEvents)
            stateJob.cancel()
            navJob.cancel()
        }

    @Test
    fun createAlbum_givenFailure_whenCompletes_thenErrorSetAndSheetModeUnchanged() =
        runTest(testDispatcher) {
            val viewModel = homeViewModel(createAlbumUseCase = failureCreateUseCase("err"))
            val states = mutableListOf<HomeUiState>()
            val stateJob = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            viewModel.showCreateSheet()
            advanceUntilIdle()
            viewModel.createAlbum("New Album", null)
            advanceUntilIdle()

            assertEquals("err", states.last().error)
            assertEquals(AlbumSheetMode.Create, states.last().sheetMode)
            stateJob.cancel()
        }

    @Test
    fun updateAlbum_givenSuccess_whenCompletes_thenSheetHidden() =
        runTest(testDispatcher) {
            val viewModel = homeViewModel(updateAlbumUseCase = successUpdateUseCase())
            val states = mutableListOf<HomeUiState>()
            val stateJob = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            viewModel.showEditSheet(album(1L))
            advanceUntilIdle()
            viewModel.updateAlbum(1L, "Updated", null)
            advanceUntilIdle()

            assertEquals(AlbumSheetMode.Hidden, states.last().sheetMode)
            stateJob.cancel()
        }

    @Test
    fun updateAlbum_givenFailure_whenCompletes_thenErrorSet() =
        runTest(testDispatcher) {
            val viewModel = homeViewModel(updateAlbumUseCase = failureUpdateUseCase("err"))
            val states = mutableListOf<HomeUiState>()
            val stateJob = launch { viewModel.uiState.collect { states.add(it) } }
            advanceUntilIdle()

            viewModel.showEditSheet(album(1L))
            advanceUntilIdle()
            viewModel.updateAlbum(1L, "Updated", null)
            advanceUntilIdle()

            assertEquals("err", states.last().error)
            stateJob.cancel()
        }

    // endregion

    // region — helpers

    private fun homeViewModel(
        getAlbumsUseCase: GetAlbumsUseCase = GetAlbumsUseCase(FakeAlbumRepository(flowOf())),
        createAlbumUseCase: CreateAlbumUseCase = successCreateUseCase(0L),
        updateAlbumUseCase: UpdateAlbumUseCase = successUpdateUseCase(),
    ) = HomeViewModel(getAlbumsUseCase, createAlbumUseCase, updateAlbumUseCase)

    private fun successCreateUseCase(id: Long) =
        CreateAlbumUseCase(stubRepo(createAlbum = { _, _ -> id }))

    private fun failureCreateUseCase(error: String) =
        CreateAlbumUseCase(stubRepo(createAlbum = { _, _ -> throw RuntimeException(error) }))

    private fun successUpdateUseCase() =
        UpdateAlbumUseCase(stubRepo(albumFlow = flowOf(album(1L))))

    private fun failureUpdateUseCase(error: String) =
        UpdateAlbumUseCase(stubRepo(albumFlow = flowOf(album(1L)), onUpdate = { throw RuntimeException(error) }))

    private fun stubRepo(
        albumFlow: Flow<Album?> = flowOf(null),
        createAlbum: suspend (String, String?) -> Long = { _, _ -> 0L },
        onUpdate: suspend (Album) -> Unit = {},
    ) = object : AlbumRepository {
        override fun getAllAlbums(): Flow<List<Album>> = flowOf(emptyList())
        override fun getAlbumById(albumId: Long): Flow<Album?> = albumFlow
        override suspend fun createAlbum(name: String, description: String?) = createAlbum(name, description)
        override suspend fun updateAlbum(album: Album) = onUpdate(album)
        override suspend fun deleteAlbum(album: Album) = error("unused")
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
