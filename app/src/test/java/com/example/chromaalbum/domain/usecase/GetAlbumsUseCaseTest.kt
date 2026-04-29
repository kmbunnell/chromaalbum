package com.example.chromaalbum.domain.usecase

import app.cash.turbine.test
import com.example.chromaalbum.domain.FakeAlbumRepository
import com.example.chromaalbum.domain.ThrowingAlbumRepository
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetAlbumsUseCaseTest {
    @Test
    fun invoke_givenEmptyList_whenCollected_thenEmitsSuccessWithEmptyAlbums() =
        runTest {
            val fakeFlow = MutableSharedFlow<List<Album>>(replay = 1)
            val useCase = GetAlbumsUseCase(FakeAlbumRepository(fakeFlow))

            fakeFlow.emit(emptyList())

            useCase().test {
                val item = awaitItem()
                assertTrue(item is Result.Success)
                assertTrue((item as Result.Success).data.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun invoke_givenThreeAlbums_whenCollected_thenEmitsSuccessWithSizeThree() =
        runTest {
            val fakeFlow = MutableSharedFlow<List<Album>>(replay = 1)
            val useCase = GetAlbumsUseCase(FakeAlbumRepository(fakeFlow))

            fakeFlow.emit(listOf(album(1), album(2), album(3)))

            useCase().test {
                val item = awaitItem()
                assertTrue(item is Result.Success)
                assertEquals(3, (item as Result.Success).data.size)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun invoke_givenRepositoryThrows_whenCollected_thenEmitsFailureWithMessage() =
        runTest {
            val useCase = GetAlbumsUseCase(ThrowingAlbumRepository())

            useCase().test {
                val item = awaitItem()
                assertTrue(item is Result.Failure)
                assertEquals("db error", (item as Result.Failure).error)
                cancelAndIgnoreRemainingEvents()
            }
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
