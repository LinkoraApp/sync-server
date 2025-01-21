package com.sakethh.linkora

import com.sakethh.linkora.domain.model.ServerConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
                val invalidCharText = listOf(
                    "\n\nWas waiting for a 'P', but since you're too cool for that, crashing the setup like it's no big deal. Enjoy the chaos! 💥\n",
                    "\n\nExpected a nice 'P' to play along, but since we’re not on the same page, crashing the setup without hesitation! 😏\n",
                    "\n\nWaiting for a 'P' to keep things moving, but since it’s missing... BOOM, setup crash incoming! 💣\n",
                    "\n\nWas hoping for a friendly 'P', but nope—no mercy! Forcing a crash now. You asked for it! 😜",
                    "\n\nYou didn’t hit me with that 'P'? Guess what? Setup crashing like it’s a Nas verse—smooth but hard-hitting. Ain’t no mercy, fam. 🔨\n",
                    "\n\nNo 'P'? You know what that means? Time to crash this setup like Nas’ flow—effortless but packs a punch. Get ready for it. ⚡\n"
                )
                println(invalidCharText.random())
                throw IllegalArgumentException()
            }
        }
    }

    fun readConfig(): ServerConfig {
        createConfig(forceWrite = false)
        return Files.readString(configFilePath).let {
            try {
                json.decodeFromString<ServerConfig>(it)
            } catch (_: Exception) {
                println("It seems you’ve manipulated `linkoraConfig.json` and messed things up a bit. No problemo, we’ll restart the configuration process to make sure things go smoothly.")
                createConfig(forceWrite = true)
                readConfig()
            }
        }
    }
}

fun Application.module() {
    configureDatabase()
    configureSecurity()
    configureSerialization()
    configureRouting()
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }
    configureWebSocket()
}
