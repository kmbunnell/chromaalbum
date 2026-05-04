package com.example.chromaalbum.data.palette

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import coil3.ImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.CachePolicy
import coil3.request.allowHardware
import coil3.size.Size
import coil3.toBitmap
import com.example.chromaalbum.di.DefaultDispatcher
import com.example.chromaalbum.domain.palette.PhotoLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PhotoLoaderImpl
    @Inject
    constructor(
        private val imageLoader: ImageLoader,
        @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
        @param:ApplicationContext private val context: Context,
    ) : PhotoLoader {
        override suspend fun load(uri: Uri): Bitmap =
            withContext(dispatcher) {
                val request =
                    ImageRequest
                        .Builder(context)
                        .data(uri)
                        .size(Size(100, 100))
                        .allowHardware(false)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .build()
                when (val result = imageLoader.execute(request)) {
                    is SuccessResult -> result.image.toBitmap()
                    is ErrorResult -> throw IllegalStateException("Failed to load photo", result.throwable)
                }
            }
    }
