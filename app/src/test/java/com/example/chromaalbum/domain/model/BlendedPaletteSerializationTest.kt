package com.example.chromaalbum.domain.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BlendedPaletteSerializationTest {

    private val fullPalette = BlendedPalette(
        vibrant = SwatchData(hex = "#FF0000", titleText = "#FFFFFF"),
        darkVibrant = SwatchData(hex = "#AA0000", titleText = "#FFFFFF"),
        lightVibrant = SwatchData(hex = "#FF6666", titleText = "#000000"),
        muted = SwatchData(hex = "#CC5555", titleText = "#FFFFFF"),
        darkMuted = SwatchData(hex = "#883333", titleText = "#FFFFFF"),
        lightMuted = SwatchData(hex = "#FFAAAA", titleText = "#000000"),
    )

    @Test
    fun toJson_givenFullPalette_whenCalled_thenJsonContainsSixSwatchKeysWithHexAndTitleText() {
        // When
        val json = Json.encodeToString(fullPalette)
        val root = Json.parseToJsonElement(json).jsonObject

        // Then
        assertEquals(6, root.size)
        val swatchKeys = setOf("vibrant", "darkVibrant", "lightVibrant", "muted", "darkMuted", "lightMuted")
        assertEquals(swatchKeys, root.keys)
        root.values.forEach { element ->
            val obj = element.jsonObject
            assertNotNull(obj["hex"])
            assertNotNull(obj["titleText"])
        }
    }

    @Test
    fun toJson_fromJson_givenPaletteWithNullSwatches_whenRoundTripped_thenNullsPreserved() {
        // Given
        val palette = BlendedPalette(
            vibrant = SwatchData(hex = "#FF0000", titleText = "#FFFFFF"),
            darkVibrant = null,
            lightVibrant = null,
            muted = SwatchData(hex = "#CC5555", titleText = "#FFFFFF"),
            darkMuted = null,
            lightMuted = null,
        )

        // When
        val result = Json.decodeFromString<BlendedPalette>(Json.encodeToString(palette))

        // Then
        assertEquals(palette, result)
    }
}
