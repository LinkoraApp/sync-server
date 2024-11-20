package com.sakethh.linkora

import com.sakethh.linkora.domain.model.ServerConfig
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.time.Duration.Companion.seconds

suspend fun main() {
    val serverConfig = ServerConfiguration.readConfig()
    embeddedServer(
        Netty, port = serverConfig.serverPort, host = serverConfig.hostAddress,
        module = Application::module
    ).start(wait = true)
}

object ServerConfiguration {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    private val jarDir = Paths.get(this::class.java.protectionDomain.codeSource.location.toURI()).parent
    private val configFilePath = jarDir.resolve("linkoraConfig.json")

    private fun doesConfigFileExists(): Boolean {
        return Files.exists(configFilePath)
    }

    private fun createConfig() {
        if (doesConfigFileExists().not()) {
            Files.createFile(configFilePath)
            resetConfig()
        }
    }

    fun resetConfig() {
        val jsonConfigString = json.encodeToString(ServerConfig())
        println(jsonConfigString)
        Files.writeString(configFilePath, jsonConfigString, StandardOpenOption.TRUNCATE_EXISTING)
    }

    fun readConfig(): ServerConfig {
        createConfig()
        return Files.readString(configFilePath).let {
            json.decodeFromString<ServerConfig>(it)
        }
    }
}

fun Application.module() {
    ServerConfiguration.readConfig().let { serverConfig ->
        configureDatabase(
            url = serverConfig.databaseUrl, user = serverConfig.databaseUser, password = serverConfig.databasePassword
        )
    }
    configureSecurity()
    configureSerialization()
    configureAdministration()
    configureRouting()
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    configureWebSocket()
}
