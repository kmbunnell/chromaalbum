package com.example.chromaalbum.data.mapper

import com.example.chromaalbum.data.local.entity.PhotoEntity
import com.example.chromaalbum.domain.model.Photo

fun PhotoEntity.toDomain(): Photo =
    Photo(
        id = id,
        albumId = albumId,
        uri = uri,
        addedAt = addedAt,
        sortOrder = sortOrder,
    )

fun Photo.toEntity(): PhotoEntity =
    PhotoEntity(
        id = id,
        albumId = albumId,
        uri = uri,
        addedAt = addedAt,
        sortOrder = sortOrder,
    )
