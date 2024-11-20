package com.sakethh.linkora

import com.sakethh.linkora.LinkoraWebSocket.initializeWriteChannel
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

fun Application.configureWebSocket() {
    routing {
        webSocket("/events") {
            initializeWriteChannel()
            try {
                for (frame in incoming) {
                }
            } catch (e: Exception) {
                println("WebSocket error: ${e.message}")
            } finally {
                LinkoraWebSocket.closeWriteChannel()
                println("WebSocket closed.")
            }
        }
    }
}