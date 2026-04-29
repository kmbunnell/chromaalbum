package com.example.chromaalbum.ui.screens

import com.example.chromaalbum.domain.model.Album

data class HomeUiState(
    val albums: List<Album> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
