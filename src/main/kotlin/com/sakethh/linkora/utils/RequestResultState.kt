package com.sakethh.linkora.utils

sealed class RequestResultState<T> {
    data class Success<T>(val result: T) : RequestResultState<T>()
    data class Failure<T>(val exception: Exception) : RequestResultState<T>()
}