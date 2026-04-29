package com.example.chromaalbum.ui.screens

import com.example.chromaalbum.domain.FakeAlbumRepository
import com.example.chromaalbum.domain.ThrowingAlbumRepository
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.usecase.GetAlbumsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun uiState_givenNoEmissions_whenCreated_thenIsLoading() =
        runTest(testDispatcher) {
            val viewModel = HomeViewModel(GetAlbumsUseCase(FakeAlbumRepository(MutableSharedFlow())))
            val states = mutableListOf<HomeUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }

            advanceUntilIdle()

            assertTrue(states.first().isLoading)
            assertTrue(states.first().albums.isEmpty())
            assertNull(states.first().error)
            job.cancel()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun uiState_givenAlbumsEmitted_whenCollected_thenAlbumsPopulatedAndNotLoading() =
        runTest(testDispatcher) {
            val fakeFlow = MutableSharedFlow<List<Album>>(replay = 1)
            fakeFlow.emit(listOf(album(1), album(2)))
            val viewModel = HomeViewModel(GetAlbumsUseCase(FakeAlbumRepository(fakeFlow)))
            val states = mutableListOf<HomeUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }

            advanceUntilIdle()

            val success = states.last()
            assertFalse(success.isLoading)
            assertEquals(2, success.albums.size)
            assertNull(success.error)
            job.cancel()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun uiState_givenRepositoryErrors_whenCollected_thenErrorNotNullAndNotLoading() =
        runTest(testDispatcher) {
            val viewModel = HomeViewModel(GetAlbumsUseCase(ThrowingAlbumRepository()))
            val states = mutableListOf<HomeUiState>()
            val job = launch { viewModel.uiState.collect { states.add(it) } }

            advanceUntilIdle()

            val error = states.last()
            assertNotNull(error.error)
            assertFalse(error.isLoading)
            job.cancel()
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

}
