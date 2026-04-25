package com.example.chromaalbum.data.mapper

import com.example.chromaalbum.data.local.entity.AlbumEntity
import com.example.chromaalbum.domain.model.Album
import kotlinx.serialization.json.Json

fun AlbumEntity.toDomain(): Album =
    Album(
        id = id,
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        coverPhotoUri = coverPhotoUri,
        dominantColor = dominantColor,
        palette = Json.decodeFromString(paletteJson),
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
        paletteJson = Json.encodeToString(palette),
    )
