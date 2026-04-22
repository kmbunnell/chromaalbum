package com.example.chromaalbum.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SwatchData(
    val hex: String,
    val titleText: String,
)

@Serializable
data class BlendedPalette(
    val vibrant: SwatchData?,
    val darkVibrant: SwatchData?,
    val lightVibrant: SwatchData?,
    val muted: SwatchData?,
    val darkMuted: SwatchData?,
    val lightMuted: SwatchData?,
) {
    fun isEmpty() = listOf(vibrant, darkVibrant, lightVibrant, muted, darkMuted, lightMuted).all { it == null }

    fun dominantHex(): String? =
        vibrant?.hex
            ?: listOf(darkVibrant, lightVibrant, muted, darkMuted, lightMuted).firstNotNullOfOrNull { it?.hex }
}
