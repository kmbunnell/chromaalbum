package com.example.chromaalbum.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.usecase.GetAlbumUseCase
import com.example.chromaalbum.domain.usecase.GetPhotosForAlbumUseCase
import com.example.chromaalbum.domain.usecase.SaveNewAlbumUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumEditUiState(
    val isCreateMode: Boolean,
    val name: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val photos: List<Photo> = emptyList(),
    val displayUris: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val navigateUp: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AlbumEditViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getAlbumUseCase: GetAlbumUseCase,
        private val getPhotosForAlbumUseCase: GetPhotosForAlbumUseCase,
        private val saveNewAlbumUseCase: SaveNewAlbumUseCase,
    ) : ViewModel() {
        private val albumId: Long? = savedStateHandle["albumId"]

        private val _uiState =
            MutableStateFlow(
                AlbumEditUiState(isCreateMode = albumId == null, isLoading = albumId != null),
            )
        val uiState: StateFlow<AlbumEditUiState> = _uiState.asStateFlow()

        init {
            if (albumId != null) {
                viewModelScope.launch {
                    combine(
                        getAlbumUseCase(albumId),
                        getPhotosForAlbumUseCase(albumId),
                    ) { albumResult, photosResult ->
                        when {
                            albumResult is Result.Failure ->
                                _uiState.value.copy(isLoading = false, error = albumResult.error)
                            photosResult is Result.Failure ->
                                _uiState.value.copy(isLoading = false, error = (photosResult as Result.Failure).error)
                            else -> {
                                val album = (albumResult as Result.Success).data
                                val photos = (photosResult as Result.Success).data
                                _uiState.value.copy(
                                    isLoading = false,
                                    name = album.name,
                                    description = album.description ?: "",
                                    photos = photos,
                                    displayUris = photos.map { it.uri },
                                    error = null,
                                )
                            }
                        }
                    }.collect { newState -> _uiState.update { newState } }
                }
            }
        }

        fun onNameChanged(name: String) {
            _uiState.update { it.copy(name = name) }
        }

        fun onDescriptionChanged(desc: String) {
            _uiState.update { it.copy(description = desc) }
        }

        fun onPhotosSelected(uris: List<String>) {
            _uiState.update { it.copy(displayUris = it.displayUris + uris) }
        }

        fun onDone() {
            if (!_uiState.value.isCreateMode) return
            val state = _uiState.value
            _uiState.update { it.copy(isSaving = true) }
            viewModelScope.launch {
                saveNewAlbumUseCase(state.name, state.description.takeIf { it.isNotBlank() }, state.displayUris)
                    .collect { result ->
                        when (result) {
                            is Result.Success ->
                                _uiState.update { it.copy(isSaving = false, navigateUp = true) }
                            is Result.Failure ->
                                _uiState.update { it.copy(isSaving = false, error = result.error) }
                        }
                    }
            }
        }

        fun onNavigatedUp() {
            _uiState.update { it.copy(navigateUp = false) }
        }
    }
