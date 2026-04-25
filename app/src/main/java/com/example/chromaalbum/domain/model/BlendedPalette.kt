package com.example.chromaalbum.domain.model

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SwatchData(
    val hex: String,
    val titleText: String,
)

private val paletteJson = Json { ignoreUnknownKeys = true }

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

    fun toJson(): String = paletteJson.encodeToString(this)

    companion object {
        fun fromJson(jsonString: String): BlendedPalette? =
            try {
                paletteJson.decodeFromString(jsonString)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
    }
}
