package com.example.chromaalbum.ui.screens

import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.ui.components.AlbumSheetMode

data class HomeUiState(
    val albums: List<Album> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sheetMode: AlbumSheetMode = AlbumSheetMode.Hidden,
    val pendingNavigation: Long? = null,
)
