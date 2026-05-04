package com.example.chromaalbum.domain.model

data class Album(
    val id: Long = 0,
    val name: String,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val dominantColor: String? = null,
    val palette: BlendedPalette = BlendedPalette(),
)
