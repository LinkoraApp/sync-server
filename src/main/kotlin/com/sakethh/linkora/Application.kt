package com.sakethh.linkora

import com.sakethh.linkora.data.configureDatabase
import com.sakethh.linkora.data.repository.MarkdownManagerRepoImpl
import com.sakethh.linkora.domain.model.ServerConfig
import com.sakethh.linkora.domain.repository.MarkdownManagerRepo
import com.sakethh.linkora.domain.routes.SyncRoute
import com.sakethh.linkora.presentation.routing.configureRouting
import com.sakethh.linkora.presentation.routing.websocket.configureEventsWebSocket
import com.sakethh.linkora.utils.SysEnvKey
import com.sakethh.linkora.utils.useSysEnvValues
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.time.Duration.Companion.seconds

fun main() {
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

    fun createConfig(forceWrite: Boolean) {
        if (doesConfigFileExists().not() || forceWrite) {
            if (forceWrite.not()) {
                println("The required configuration file does not exist. Proceeding to create a new one.")
            }
            if (doesConfigFileExists().not()) {
                Files.createFile(configFilePath)
            }
            if (forceWrite.not()) {
                println("A configuration file named `linkoraConfig.json` has been created. Do not change the name or extension.")
            }
            println("Press 'P' (should be in uppercase) and hit Enter to start the setup, which will proceed with the configuration.")
            val inputChar = readln()
            if (inputChar == "P") {

                println("Enter the database URL, and make sure the database is running:")
                val dataBaseUrl = readln()
                println("Enter the database username:")
                val dataBaseUserName = readln()
                println("Enter the database password:")
                val dataBasePassword = readln()
                println("Enter the Auth Token. This will be used by the Linkora app and allow other clients to change the data.\nKeep this token long and strong, and DO NOT SHARE IT, \nas it grants access to the Linkora database and server, allowing full control over it:")
                val serverAuthToken = readln()

                val serverConfig = ServerConfig(
                    databaseUrl = if (dataBaseUrl.endsWith("/linkora").not()) "$dataBaseUrl/linkora" else dataBaseUrl,
                    databaseUser = dataBaseUserName,
                    databasePassword = dataBasePassword,
                    serverAuthToken = serverAuthToken
                )
                val jsonConfigString = json.encodeToString(serverConfig)
                println(jsonConfigString)
                Files.writeString(configFilePath, jsonConfigString, StandardOpenOption.TRUNCATE_EXISTING)
                println("Successfully configured the server with the given data.")
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    fun readConfig(): ServerConfig {
        return if (useSysEnvValues()) {
            ServerConfig(
                databaseUrl = "jdbc:" + System.getenv(SysEnvKey.LINKORA_DATABASE_URL.name),
                databaseUser = System.getenv(SysEnvKey.LINKORA_DATABASE_USER.name),
                databasePassword = System.getenv(SysEnvKey.LINKORA_DATABASE_PASSWORD.name), hostAddress = try {
                    // manually throw the exception as `getenv` may return null, and no conversion is happening here to auto-throw
                    System.getenv(SysEnvKey.LINKORA_HOST_ADDRESS.name) ?: throw NullPointerException()
                } catch (_: Exception) {
                    "0.0.0.0"
                }, serverPort = try {
                    System.getenv(SysEnvKey.LINKORA_SERVER_PORT.name).toInt()
                } catch (_: Exception) {
                    8080
                },
                serverAuthToken = System.getenv(SysEnvKey.LINKORA_SERVER_AUTH_TOKEN.name)
            )
        } else {
            createConfig(forceWrite = false)
            Files.readString(configFilePath).let {
                try {
                    json.decodeFromString<ServerConfig>(it).let {
                        it.copy(databaseUrl = "jdbc:" + it.databaseUrl)
                    }
                } catch (_: Exception) {
                    println("It seems you’ve manipulated `linkoraConfig.json` and messed things up a bit. No problemo, we’ll restart the configuration process to make sure things go smoothly.")
                    createConfig(forceWrite = true)
                    readConfig()
                }
            }
        }
    }
}

fun Application.module() {
    println("The server version is ${Constants.SERVER_VERSION}")
    configureDatabase()
    configureSecurity()
    configureSerialization()
    val mdManagerRepo : MarkdownManagerRepo = MarkdownManagerRepoImpl()
    val serverConfig =ServerConfiguration.readConfig()
    configureRouting(serverConfig = serverConfig, markdownManagerRepo = mdManagerRepo)
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
    }
    configureEventsWebSocket()
    val serverConfiguredPage =
        "http://" + serverConfig.hostAddress + ":" + serverConfig.serverPort + "/" + SyncRoute.SERVER_IS_CONFIGURED.name
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI(serverConfiguredPage))
    }
}
