package com.example.chromaalbum.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.usecase.DeleteAlbumUseCase
import com.example.chromaalbum.domain.usecase.GetAlbumsUseCase
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
        private val deleteAlbumUseCase: DeleteAlbumUseCase,
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

        fun clearError() {
            _uiState.update { it.copy(error = null) }
        }

        fun requestDeleteAlbum(album: Album) {
            _uiState.update { it.copy(albumPendingDelete = album) }
        }

        fun dismissDeleteDialog() {
            _uiState.update { it.copy(albumPendingDelete = null) }
        }

        fun confirmDeleteAlbum() {
            val album = _uiState.value.albumPendingDelete ?: return
            viewModelScope.launch {
                deleteAlbumUseCase(album).collect { result ->
                    when (result) {
                        is Result.Success -> _uiState.update { it.copy(albumPendingDelete = null) }
                        is Result.Failure ->
                            _uiState.update { it.copy(error = result.error, albumPendingDelete = null) }
                    }
                }
            }
        }
    }
