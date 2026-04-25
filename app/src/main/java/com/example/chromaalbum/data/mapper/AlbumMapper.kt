package com.example.chromaalbum.data.mapper

import com.example.chromaalbum.data.local.entity.AlbumEntity
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.domain.model.BlendedPalette

fun AlbumEntity.toDomain(): Album =
    Album(
        id = id,
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        coverPhotoUri = coverPhotoUri,
        dominantColor = dominantColor,
        palette = BlendedPalette.fromJson(paletteJson) ?: BlendedPalette(),
    )

fun Album.toEntity(): AlbumEntity =
    AlbumEntity(
        id = id,
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        coverPhotoUri = coverPhotoUri,
        dominantColor = palette.dominantHex(),
        paletteJson = palette.toJson(),
    )
