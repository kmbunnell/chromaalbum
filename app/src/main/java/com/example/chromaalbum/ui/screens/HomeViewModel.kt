package com.example.chromaalbum.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.usecase.CreateAlbumUseCase
import com.example.chromaalbum.domain.usecase.GetAlbumsUseCase
import com.example.chromaalbum.domain.usecase.UpdateAlbumUseCase
import com.example.chromaalbum.ui.components.AlbumSheetMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val getAlbumsUseCase: GetAlbumsUseCase,
        private val createAlbumUseCase: CreateAlbumUseCase,
        private val updateAlbumUseCase: UpdateAlbumUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                getAlbumsUseCase().collect { result ->
                    _uiState.update { state ->
                        when (result) {
                            is Result.Success -> state.copy(albums = result.data, isLoading = false)
                            is Result.Failure -> state.copy(error = result.error, isLoading = false)
                        }
                    }
                }
            }
        }

        fun showCreateSheet() {
            _uiState.update { it.copy(sheetMode = AlbumSheetMode.Create) }
        }

        fun showEditSheet(album: Album) {
            _uiState.update { it.copy(sheetMode = AlbumSheetMode.Edit(album)) }
        }

        fun dismissSheet() {
            _uiState.update { it.copy(sheetMode = AlbumSheetMode.Hidden, error = null) }
        }

        fun clearError() {
            _uiState.update { it.copy(error = null) }
        }

        fun consumeNavigation() {
            _uiState.update { it.copy(pendingNavigation = null) }
        }

        fun createAlbum(
            name: String,
            description: String?,
        ) {
            _uiState.update { it.copy(error = null) }
            viewModelScope.launch {
                createAlbumUseCase(name, description).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(sheetMode = AlbumSheetMode.Hidden, pendingNavigation = result.data)
                            }
                        }
                        is Result.Failure -> _uiState.update { it.copy(error = result.error) }
                    }
                }
            }
        }

        fun updateAlbum(
            id: Long,
            name: String,
            description: String?,
        ) {
            _uiState.update { it.copy(error = null) }
            viewModelScope.launch {
                updateAlbumUseCase(id, name, description).collect { result ->
                    when (result) {
                        is Result.Success -> _uiState.update { it.copy(sheetMode = AlbumSheetMode.Hidden) }
                        is Result.Failure -> _uiState.update { it.copy(error = result.error) }
                    }
                }
            }
        }
    }
