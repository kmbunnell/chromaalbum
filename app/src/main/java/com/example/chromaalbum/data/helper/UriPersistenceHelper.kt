package com.example.chromaalbum.data.helper

import android.net.Uri

interface UriPersistenceHelper {
    fun persist(uri: Uri)

    fun release(uri: Uri)
}
