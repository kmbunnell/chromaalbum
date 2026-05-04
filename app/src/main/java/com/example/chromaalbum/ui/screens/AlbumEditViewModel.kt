package com.example.chromaalbum.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chromaalbum.domain.model.Photo
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.usecase.GetAlbumUseCase
import com.example.chromaalbum.domain.usecase.GetPhotosForAlbumUseCase
import com.example.chromaalbum.domain.usecase.SaveEditedAlbumUseCase
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
    val pendingUris: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val navigateUp: Boolean = false,
    val error: String? = null,
) {
    val displayUris: List<String>
        get() =
            if (isCreateMode) {
                pendingUris
            } else {
                val persisted = photos.map { it.uri }
                val persistedSet = persisted.toHashSet()
                persisted + pendingUris.filter { it !in persistedSet }
            }
}

@HiltViewModel
class AlbumEditViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getAlbumUseCase: GetAlbumUseCase,
        private val getPhotosForAlbumUseCase: GetPhotosForAlbumUseCase,
        private val saveNewAlbumUseCase: SaveNewAlbumUseCase,
        private val saveEditedAlbumUseCase: SaveEditedAlbumUseCase,
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
            _uiState.update { it.copy(pendingUris = it.pendingUris + uris) }
        }

        fun onDone() {
            val state = _uiState.value
            _uiState.update { it.copy(isSaving = true) }
            viewModelScope.launch {
                if (state.isCreateMode) {
                    saveNewAlbumUseCase(
                        state.name,
                        state.description.takeIf { it.isNotBlank() },
                        state.pendingUris,
                    ).collect { onSaveResult(it) }
                } else {
                    val id = albumId ?: return@launch
                    saveEditedAlbumUseCase(
                        id,
                        state.name,
                        state.description.takeIf { it.isNotBlank() },
                        state.pendingUris,
                        state.displayUris,
                    ).collect { onSaveResult(it) }
                }
            }
        }

        private fun onSaveResult(result: Result<*, String>) {
            when (result) {
                is Result.Success -> _uiState.update { it.copy(navigateUp = true) }
                is Result.Failure -> _uiState.update { it.copy(isSaving = false, error = result.error) }
            }
        }

        fun onNavigatedUp() {
            _uiState.update { it.copy(navigateUp = false, isSaving = false) }
        }
    }
