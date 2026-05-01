package com.example.chromaalbum.ui.screens

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.chromaalbum.domain.model.Album
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val stubAlbum =
        Album(
            id = 1L,
            name = "Test Album",
            description = null,
            createdAt = 0L,
            updatedAt = 0L,
            coverPhotoUri = null,
        )

    @Test
    fun content_givenEmptyAlbumsAndNotLoading_whenRendered_thenEmptyStateTextDisplayed() {
        composeTestRule.setContent {
            HomeContent(
                uiState = HomeUiState(albums = emptyList(), isLoading = false),
                onAlbumClick = {},
                onCreateAlbum = {},
                onDismissSheet = {},
                onSaveCreate = { _, _ -> },
                onSaveEdit = { _, _, _ -> },
                onErrorDismissed = {},
            )
        }

        composeTestRule.onNodeWithText("Create your first album").assertIsDisplayed()
    }

    @Test
    fun content_givenAlbumsPresent_whenRendered_thenEmptyStateTextNotDisplayed() {
        composeTestRule.setContent {
            HomeContent(
                uiState = HomeUiState(albums = listOf(stubAlbum), isLoading = false),
                onAlbumClick = {},
                onCreateAlbum = {},
                onDismissSheet = {},
                onSaveCreate = { _, _ -> },
                onSaveEdit = { _, _, _ -> },
                onErrorDismissed = {},
            )
        }

        composeTestRule.onNodeWithText("Create your first album").assertIsNotDisplayed()
    }

    @Test
    fun content_givenLoading_whenRendered_thenEmptyStateNotDisplayed() {
        composeTestRule.setContent {
            HomeContent(
                uiState = HomeUiState(albums = emptyList(), isLoading = true),
                onAlbumClick = {},
                onCreateAlbum = {},
                onDismissSheet = {},
                onSaveCreate = { _, _ -> },
                onSaveEdit = { _, _, _ -> },
                onErrorDismissed = {},
            )
        }

        composeTestRule.onNodeWithText("Create your first album").assertIsNotDisplayed()
    }

    @Test
    fun homeContent_given5Albums_whenRendered_then5AlbumCardNodesPresent() {
        val fiveAlbums =
            (1..5).map { i ->
                Album(
                    id = i.toLong(),
                    name = "Album $i",
                    description = null,
                    createdAt = 0L,
                    updatedAt = 0L,
                    coverPhotoUri = null,
                )
            }

        composeTestRule.setContent {
            HomeContent(
                uiState = HomeUiState(albums = fiveAlbums, isLoading = false),
                onAlbumClick = {},
                onCreateAlbum = {},
                onDismissSheet = {},
                onSaveCreate = { _, _ -> },
                onSaveEdit = { _, _, _ -> },
                onErrorDismissed = {},
            )
        }

        composeTestRule.onAllNodesWithTag("album_card").assertCountEquals(5)
    }

    @Test
    fun homeContent_givenAlbum_whenCardTapped_thenOnAlbumClickCalledWithAlbumId() {
        var capturedId: Long? = null
        composeTestRule.setContent {
            HomeContent(
                uiState = HomeUiState(albums = listOf(stubAlbum), isLoading = false),
                onAlbumClick = { capturedId = it },
                onCreateAlbum = {},
                onDismissSheet = {},
                onSaveCreate = { _, _ -> },
                onSaveEdit = { _, _, _ -> },
                onErrorDismissed = {},
            )
        }

        composeTestRule.onNodeWithTag("album_card").performClick()

        assertEquals(stubAlbum.id, capturedId)
    }

    @Test
    fun homeContent_givenError_whenRendered_thenSnackbarDisplayed() {
        composeTestRule.setContent {
            HomeContent(
                uiState = HomeUiState(error = "Something went wrong", isLoading = false),
                onAlbumClick = {},
                onCreateAlbum = {},
                onDismissSheet = {},
                onSaveCreate = { _, _ -> },
                onSaveEdit = { _, _, _ -> },
                onErrorDismissed = {},
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
    }
}
