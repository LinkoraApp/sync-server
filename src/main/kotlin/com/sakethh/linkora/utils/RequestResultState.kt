package com.sakethh.linkora.utils

import io.ktor.http.*

sealed class RequestResultState<T> {
    data class Success<T>(val result: T) : RequestResultState<T>()
    data class Failure<T>(
        val exception: Exception,
        val httpStatusCode: HttpStatusCode = HttpStatusCode.InternalServerError
    ) : RequestResultState<T>()
}

suspend fun <T> RequestResultState<T>.onSuccess(init: suspend (result: T) -> Unit) {
    this as RequestResultState.Success<T>
    init(this.result)
}

suspend fun <T> RequestResultState<T>.onFailure(init: suspend (exception: Exception, httpStatusCode: HttpStatusCode) -> Unit) {
    this as RequestResultState.Failure<T>
    init(this.exception, this.httpStatusCode)
}