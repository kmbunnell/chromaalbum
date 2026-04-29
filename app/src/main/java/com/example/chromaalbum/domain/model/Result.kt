package com.example.chromaalbum.domain.model

sealed class Result<out S, out F> {
    data class Success<out S>(
        val data: S,
    ) : Result<S, Nothing>()

    data class Failure<out F>(
        val error: F,
    ) : Result<Nothing, F>()
}
