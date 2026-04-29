package com.example.chromaalbum.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chromaalbum.domain.model.Result
import com.example.chromaalbum.domain.usecase.GetAlbumsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        getAlbumsUseCase: GetAlbumsUseCase,
    ) : ViewModel() {
        val uiState: StateFlow<HomeUiState> =
            getAlbumsUseCase()
                .map { result ->
                    when (result) {
                        is Result.Success -> HomeUiState(albums = result.data, isLoading = false)
                        is Result.Failure -> HomeUiState(error = result.error, isLoading = false)
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = HomeUiState(isLoading = true),
                )
    }
