package com.example.chromaalbum.domain.palette

import android.graphics.Bitmap
import android.net.Uri

interface PhotoLoader {
    suspend fun load(uri: Uri): Bitmap
}
