package com.example.chromaalbum.ui.components

import com.example.chromaalbum.domain.model.Album

sealed interface AlbumSheetMode {
    data object Hidden : AlbumSheetMode

    data object Create : AlbumSheetMode

    data class Edit(
        val album: Album,
    ) : AlbumSheetMode
}
