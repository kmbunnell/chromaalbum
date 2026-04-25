package com.example.chromaalbum.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SwatchData(
    val hex: String,
    val titleText: String,
)

@Serializable
data class BlendedPalette(
    val vibrant: SwatchData? = null,
    val darkVibrant: SwatchData? = null,
    val lightVibrant: SwatchData? = null,
    val muted: SwatchData? = null,
    val darkMuted: SwatchData? = null,
    val lightMuted: SwatchData? = null,
) {
    fun isEmpty() = listOf(vibrant, darkVibrant, lightVibrant, muted, darkMuted, lightMuted).all { it == null }

    fun dominantHex(): String? =
        vibrant?.hex
            ?: listOf(darkVibrant, lightVibrant, muted, darkMuted, lightMuted).firstNotNullOfOrNull { it?.hex }
}
