package com.example.chromaalbum.domain.model

data class Photo(
    val id: Long = 0,
    val albumId: Long,
    val uri: String,
    val addedAt: Long,
    val sortOrder: Int,
)
