package com.sakethh.linkora.utils

import com.sakethh.linkora.data.socket.manager.EventsWebSocketManager
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend inline fun <reified T> RoutingContext.respondWithResult(resultState: Result<T>) {
    when (resultState) {
        is Result.Failure -> {
            resultState.exception.printStackTrace()
            call.respond(
                status = resultState.httpStatusCode,
                message = resultState.exception.message + "\nStack Trace:\n" + resultState.exception.stackTrace.contentToString()
                    .replace(",", "\n")
            )
        }

        is Result.Success -> {
            call.respondText(
                status = HttpStatusCode.OK,
                contentType = ContentType.Application.Json,
                text = Json.encodeToString(resultState.response)
            )
            if (resultState.webSocketEvent != null) {
                EventsWebSocketManager.sendEvent(resultState.webSocketEvent)
            }
        }
    }
}