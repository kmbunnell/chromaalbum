package com.example.chromaalbum.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.example.chromaalbum.navigation.AlbumEditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AlbumEditUiState(
    val isCreateMode: Boolean,
    val name: String = "",
    val description: String = "",
)

@HiltViewModel
class AlbumEditViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val albumId: Long? = savedStateHandle.toRoute<AlbumEditRoute>().albumId

        private val _uiState = MutableStateFlow(AlbumEditUiState(isCreateMode = albumId == null))
        val uiState: StateFlow<AlbumEditUiState> = _uiState.asStateFlow()
    }
