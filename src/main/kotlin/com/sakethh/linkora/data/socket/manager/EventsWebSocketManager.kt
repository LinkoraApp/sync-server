package com.sakethh.linkora.data.socket.manager

import com.sakethh.linkora.domain.model.WebSocketEvent
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

object EventsWebSocketManager {
    private val writeChannels = ConcurrentHashMap<String, DefaultWebSocketServerSession>()
    private val json = Json {
        prettyPrint = true
    }

    fun DefaultWebSocketServerSession.initializeWriteChannel(correlationId: String) {
        writeChannels[correlationId] = this
    }

    suspend fun sendEvent(notification: WebSocketEvent) {
        writeChannels.forEach { (correlationId, session) ->
            try {
                session.send(Frame.Text(json.encodeToString(notification)))
                println("Sent event to the client : $correlationId")
            } catch (e: Exception) {
                println("removing the client $correlationId due to ${e.message}")
                closeWriteChannel(correlationId)
                e.printStackTrace()
            }
        }
    }

    fun closeWriteChannel(correlationId: String) {
        writeChannels.remove(correlationId)
    }
}