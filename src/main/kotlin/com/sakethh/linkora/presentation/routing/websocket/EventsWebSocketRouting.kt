package com.sakethh.linkora.presentation.routing.websocket

import com.sakethh.linkora.authenticate
import com.sakethh.linkora.data.socket.manager.EventsWebSocketManager.closeWriteChannel
import com.sakethh.linkora.data.socket.manager.EventsWebSocketManager.initializeWriteChannel
import com.sakethh.linkora.domain.dto.Correlation
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json

fun Application.configureEventsWebSocket() {
    routing {
        authenticate {
            webSocket(path = "/events") {
                val correlationParam = call.parameters["correlation"]
                if (correlationParam == null) {
                    this.close(
                        CloseReason(
                            message = "Expected `correlation` as an encoded JSON string via parameter, but it was not provided.",
                            code = CloseReason.Codes.CANNOT_ACCEPT
                        )
                    )
                    return@webSocket
                }
                val correlation = try {
                    Json.decodeFromString<Correlation>(correlationParam)
                } catch (e: Exception) {
                    e.printStackTrace()
                    this.close(
                        CloseReason(
                            message = "The schema of the provided JSON does not match the expected format.",
                            code = CloseReason.Codes.CANNOT_ACCEPT
                        )
                    )
                    return@webSocket
                }
                val correlationId = correlation.id
                initializeWriteChannel(correlationId)
                println("Established the `events` socket connection with \"${correlation.clientName}\".")
                try {
                    for (frame in incoming) {
                    }
                } catch (e: Exception) {
                    println("WebSocket error for \"${correlation.clientName}\": ${e.message}")
                    e.printStackTrace()
                } finally {
                    closeWriteChannel(correlationId)
                    println("WebSocket closed for \"${correlation.clientName}\".")
                }
            }
        }
    }
}