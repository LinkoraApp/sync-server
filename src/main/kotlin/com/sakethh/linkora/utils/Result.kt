package com.sakethh.linkora.utils

import io.ktor.http.*

sealed class Result<T> {
    data class Success<T>(val result: T) : Result<T>()
    data class Failure<T>(
        val exception: Exception,
        val httpStatusCode: HttpStatusCode = HttpStatusCode.InternalServerError
    ) : Result<T>()
}

suspend fun <T> Result<T>.onSuccess(init: suspend (result: T) -> Unit) {
    this as Result.Success<T>
    init(this.result)
}

suspend fun <T> Result<T>.onFailure(init: suspend (exception: Exception, httpStatusCode: HttpStatusCode) -> Unit) {
    this as Result.Failure<T>
    init(this.exception, this.httpStatusCode)
}