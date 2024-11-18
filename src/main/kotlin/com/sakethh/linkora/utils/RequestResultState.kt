package com.sakethh.linkora.utils

import io.ktor.http.*

sealed class RequestResultState<T> {
    data class Success<T>(val result: T) : RequestResultState<T>()
    data class Failure<T>(
        val exception: Exception,
        val httpStatusCode: HttpStatusCode = HttpStatusCode.InternalServerError
    ) : RequestResultState<T>()
}