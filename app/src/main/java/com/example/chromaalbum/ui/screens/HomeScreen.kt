package com.example.chromaalbum.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import com.example.chromaalbum.R
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.ui.components.AlbumCard
import com.example.chromaalbum.ui.components.AlbumFormSheet
import com.example.chromaalbum.ui.components.AlbumSheetMode

@Composable
fun HomeScreen(
    onAlbumClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { albumId -> onAlbumClick(albumId) }
    }

    HomeContent(
        uiState = uiState,
        onAlbumClick = onAlbumClick,
        onCreateAlbum = viewModel::showCreateSheet,
        onDismissSheet = viewModel::dismissSheet,
        onSaveCreate = { name, desc -> viewModel.createAlbum(name, desc) },
        onSaveEdit = { album, name, desc -> viewModel.updateAlbum(album.id, name, desc) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    onAlbumClick: (Long) -> Unit,
    onCreateAlbum: () -> Unit,
    onDismissSheet: () -> Unit,
    onSaveCreate: (String, String?) -> Unit,
    onSaveEdit: (Album, String, String?) -> Unit,
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val columns =
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) 2 else 3

    if (uiState.sheetMode != AlbumSheetMode.Hidden) {
        AlbumFormSheet(
            mode = uiState.sheetMode,
            onSave = { name, desc ->
                when (val mode = uiState.sheetMode) {
                    is AlbumSheetMode.Create -> onSaveCreate(name, desc)
                    is AlbumSheetMode.Edit -> onSaveEdit(mode.album, name, desc)
                    AlbumSheetMode.Hidden -> Unit
                }
            },
            onDismiss = onDismissSheet,
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateAlbum) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_album))
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.error != null -> Text(text = uiState.error.orEmpty())
                uiState.albums.isEmpty() -> EmptyState(onCreateAlbum = onCreateAlbum)
                else ->
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(columns),
                        contentPadding = PaddingValues(12.dp),
                        verticalItemSpacing = 8.dp,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(uiState.albums, key = { it.id }) { album ->
                            AlbumCard(
                                album = album,
                                onClick = { onAlbumClick(album.id) },
                                modifier = Modifier.fillMaxWidth().animateItem(),
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun EmptyState(onCreateAlbum: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.PhotoLibrary,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.home_empty_state_message))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onCreateAlbum) {
            Text(text = stringResource(R.string.create_album))
        }
    }
}
