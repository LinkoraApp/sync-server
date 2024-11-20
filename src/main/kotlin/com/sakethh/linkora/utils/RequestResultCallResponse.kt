package com.sakethh.linkora.utils

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend inline fun <reified T> RoutingContext.respondWithResult(resultState: RequestResultState<T>) {
    when (resultState) {
        is RequestResultState.Failure -> {
            call.respond(
                status = resultState.httpStatusCode,
                message = resultState.exception.message + "\nStack Trace:\n" + resultState.exception.stackTrace.contentToString()
                    .replace(",", "\n")
            )
        }

        is RequestResultState.Success -> {
            call.respondText(
                status = HttpStatusCode.OK,
                contentType = ContentType.Application.Json,
                text = Json.encodeToString(resultState.result)
            )
        }
    }
}