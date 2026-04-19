package com.example.chromaalbum.data.helper

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)
class UriPersistenceHelperImplTest {
    private class FakeContentResolver(
        private val throwOnPersist: Boolean = false,
    ) : ContentResolver(null) {
        var capturedUri: Uri? = null
        var capturedModeFlags: Int = 0
        var releasedUri: Uri? = null
        var releasedModeFlags: Int = 0

        override fun takePersistableUriPermission(
            uri: Uri,
            modeFlags: Int,
        ) {
            capturedUri = uri
            capturedModeFlags = modeFlags
            if (throwOnPersist) throw SecurityException("not persistable")
        }

        override fun releasePersistableUriPermission(
            uri: Uri,
            modeFlags: Int,
        ) {
            releasedUri = uri
            releasedModeFlags = modeFlags
        }
    }

    @Test
    fun persist_givenPersistableProvider_whenCalled_thenTakesPersistablePermissionWithReadFlag() {
        // Given
        val resolver = FakeContentResolver()
        val helper = UriPersistenceHelperImpl(resolver)
        val uri = Uri.parse("content://com.example/photo/1")

        // When
        helper.persist(uri)

        // Then
        assertEquals(uri, resolver.capturedUri)
        assertEquals(Intent.FLAG_GRANT_READ_URI_PERMISSION, resolver.capturedModeFlags)
    }

    @Test
    fun persist_givenNonPersistableProvider_whenCalled_thenThrowsUriNotPersistableExceptionWithOriginalUri() {
        // Given
        val helper = UriPersistenceHelperImpl(FakeContentResolver(throwOnPersist = true))
        val uri = Uri.parse("content://com.example/photo/42")

        // When
        val ex = assertThrows(UriNotPersistableException::class.java) { helper.persist(uri) }

        // Then
        assertEquals(uri, ex.uri)
    }

    @Test
    fun release_givenUri_whenCalled_thenReleasesPersistablePermissionWithReadFlag() {
        // Given
        val resolver = FakeContentResolver()
        val helper = UriPersistenceHelperImpl(resolver)
        val uri = Uri.parse("content://com.example/photo/1")

        // When
        helper.release(uri)

        // Then
        assertEquals(uri, resolver.releasedUri)
        assertEquals(Intent.FLAG_GRANT_READ_URI_PERMISSION, resolver.releasedModeFlags)
    }
}
