package com.sakethh.linkora

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.SendChannel
import java.util.concurrent.atomic.AtomicReference

object LinkoraWebSocket {
    private val writeChannel = AtomicReference<SendChannel<Frame>>(null)

    fun DefaultWebSocketServerSession.initializeWriteChannel() {
        writeChannel.set(this.outgoing)
    }

    suspend fun sendData(frame: Frame) {
        writeChannel.get().send(frame)
    }

    fun closeWriteChannel() {
        writeChannel.set(null)
    }
}

/*

The below code was initially implemented to listen via another port, but has now been switched to a typical generic implementation:

coroutineScope.launch {
            println("Waiting for client connection...")
            socket = aSocket(selectorManager).tcp()
                .bind(hostname = ServerConfiguration.readConfig().hostAddress, port = 9002).accept().apply {
                    println("Client connected, assigning read channel...")
                    readChannel = this.openReadChannel()
                    println("Read channel assigned successfully")

                    println("Assigning write channel for sending data...")
                    writeChannel = this.openWriteChannel(autoFlush = true)
                    println("Write channel assigned successfully")
                }
        }.invokeOnCompletion {
            CoroutineScope(Dispatchers.IO).launch {
                println("sending data")
                sendData()
            }
            println("Socket connection handling finished")
        }
* */