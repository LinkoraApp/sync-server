package com.sakethh.linkora

import com.sakethh.linkora.domain.model.WebSocketEvent
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

object WebSocketManager {
    private val writeChannels = ConcurrentHashMap<String, DefaultWebSocketServerSession>()
    private val json = Json {
        prettyPrint = true
    }

    fun DefaultWebSocketServerSession.initializeWriteChannel(sessionId: String) {
        writeChannels[sessionId] = this
    }

    suspend fun sendEvent(notification: WebSocketEvent) {
        writeChannels.forEach { (sessionId, session) ->
            try {
                session.send(Frame.Text(json.encodeToString(notification)))
                println("Sent event to the client with sessionId: $sessionId")
            } catch (e: Exception) {
                println("removing the client $sessionId due to ${e.message}")
                closeWriteChannel(sessionId)
                e.printStackTrace()
            }
        }
    }

    fun closeWriteChannel(sessionId: String) {
        writeChannels.remove(sessionId)
    }
}