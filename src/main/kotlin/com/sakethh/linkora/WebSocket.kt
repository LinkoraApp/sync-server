package com.sakethh.linkora

import com.sakethh.linkora.WebSocketManager.closeWriteChannel
import com.sakethh.linkora.WebSocketManager.initializeWriteChannel
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import java.util.*

fun Application.configureWebSocket() {
    routing {
        authenticate(Security.BEARER.name) {
            webSocket("/events") {
                val sessionId = UUID.randomUUID().toString()
                initializeWriteChannel(sessionId)
                try {
                    for (frame in incoming) {
                    }
                } catch (e: Exception) {
                    println("WebSocket error: ${e.message}")
                } finally {
                    closeWriteChannel(sessionId)
                    println("WebSocket closed.")
                }
            }
        }
    }
}