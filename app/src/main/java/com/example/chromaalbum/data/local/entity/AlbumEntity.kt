package com.example.chromaalbum.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val coverPhotoUri: String?,
    val dominantColor: String? = null,
    val paletteJson: String = EMPTY_PALETTE_JSON,
) {
    companion object {
        const val EMPTY_PALETTE_JSON =
            """{"vibrant":null,"darkVibrant":null,"lightVibrant":null,"muted":null,"darkMuted":null,"lightMuted":null}"""
    }
}
