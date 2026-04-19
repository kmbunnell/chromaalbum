package com.example.chromaalbum.data.helper

import android.net.Uri

class UriNotPersistableException(
    val uri: Uri,
    cause: Throwable,
) : Exception("Cannot persist URI permission: $uri", cause)
