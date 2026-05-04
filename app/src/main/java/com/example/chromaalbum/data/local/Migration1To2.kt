package com.example.chromaalbum.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration1To2 : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE albums_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                dominantColor TEXT,
                paletteJson TEXT NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            "INSERT INTO albums_new SELECT id, name, description, createdAt, updatedAt, dominantColor, paletteJson FROM albums",
        )
        db.execSQL("DROP TABLE albums")
        db.execSQL("ALTER TABLE albums_new RENAME TO albums")
    }
}
