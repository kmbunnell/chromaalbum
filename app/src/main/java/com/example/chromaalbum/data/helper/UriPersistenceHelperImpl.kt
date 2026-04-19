package com.example.chromaalbum.data.helper

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import javax.inject.Inject

class UriPersistenceHelperImpl
    @Inject
    constructor(
        private val contentResolver: ContentResolver,
    ) : UriPersistenceHelper {
        override fun persist(uri: Uri) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                throw UriNotPersistableException(uri, e)
            }
        }

        override fun release(uri: Uri) {
            contentResolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
