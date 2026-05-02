package com.example.chromaalbum.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data class AlbumRoute(
    val albumId: Long,
)

@Serializable
data class ViewerRoute(
    val albumId: Long,
    val startIndex: Int,
)

@Serializable
data class AlbumEditRoute(
    val albumId: Long? = null,
)
