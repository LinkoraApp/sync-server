package com.sakethh.linkora

import com.sakethh.linkora.data.configureDatabase
import com.sakethh.linkora.data.repository.MarkdownManagerRepoImpl
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.model.ServerConfig
import com.sakethh.linkora.domain.repository.MarkdownManagerRepo
import com.sakethh.linkora.presentation.routing.configureRouting
import com.sakethh.linkora.presentation.routing.websocket.configureEventsWebSocket
import com.sakethh.linkora.utils.SysEnvKey
import com.sakethh.linkora.utils.useSysEnvValues
import io.ktor.http.*
import io.ktor.network.tls.certificates.*
import io.ktor.network.tls.extensions.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.security.KeyStore
import java.util.*
import javax.security.auth.x500.X500Principal
import kotlin.io.path.exists
import kotlin.time.Duration.Companion.seconds

fun main() {
    val serverConfig = ServerConfiguration.readConfig()
    val serverKeyStore = ServerConfiguration.createOrLoadServerKeystore(
        serverConfig
    )
    require(serverConfig.keyStorePassword != null && serverConfig.keyStorePassword.isNotBlank()) {
        "keyStorePassword value must be set in ServerConfig."
    }
    ServerConfiguration.exportSignedCertificates(serverKeyStore)
    embeddedServer(
        factory = Netty, configure = {
        sslConnector(builder = {
            this.port = serverConfig.httpsPort
            this.host = serverConfig.serverHost
            enabledProtocols = listOf("TLSv1.3", "TLSv1.2")
        }, keyStore = serverKeyStore, keyAlias = Constants.KEY_STORE_ALIAS, keyStorePassword = {
            serverConfig.keyStorePassword.toCharArray()
        }, privateKeyPassword = {
            serverConfig.keyStorePassword.toCharArray()
        })

        // for http connections
        connector {
            this.port = serverConfig.httpPort
            this.host = serverConfig.serverHost
        }
    }, module = Application::module).start(wait = true)
}

val hostIp = try {
    NetworkInterface.getNetworkInterfaces().asSequence().flatMap { it.inetAddresses.asSequence() }.firstOrNull {
        !it.isLoopbackAddress && it is Inet4Address && it.isSiteLocalAddress
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}

val localhost: InetAddress = Inet4Address.getLocalHost()

object ServerConfiguration {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    val serverJarDir = Paths.get(this::class.java.protectionDomain.codeSource.location.toURI()).parent
    private val configFilePath = serverJarDir.resolve("linkoraConfig.json")

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
                    serverAuthToken = serverAuthToken,
                    keyStorePassword = ServerConfig.generateAToken()
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

    fun createConfig(serverConfig: ServerConfig) {
        require(serverConfig.keyStorePassword != null && serverConfig.keyStorePassword.isNotBlank()) {
            "keyStorePassword value must be set in ServerConfig."
        }
        val jsonConfigString = json.encodeToString(serverConfig)
        Files.writeString(configFilePath, jsonConfigString, StandardOpenOption.TRUNCATE_EXISTING)
    }

    fun readConfig(): ServerConfig {
        return if (useSysEnvValues()) {
            ServerConfig(
                databaseUrl = "jdbc:" + System.getenv(SysEnvKey.LINKORA_DATABASE_URL.name),
                databaseUser = System.getenv(SysEnvKey.LINKORA_DATABASE_USER.name),
                databasePassword = System.getenv(SysEnvKey.LINKORA_DATABASE_PASSWORD.name),
                serverHost = try {
                    // manually throw the exception as `getenv` may return null, and no conversion is happening here to auto-throw
                    System.getenv(SysEnvKey.LINKORA_HOST_ADDRESS.name) ?: throw NullPointerException()
                } catch (_: Exception) {
                    InetAddress.getLocalHost().hostAddress
                },
                httpPort = try {
                    System.getenv(SysEnvKey.LINKORA_SERVER_PORT.name).toInt()
                } catch (_: Exception) {
                    45454
                },
                httpsPort = try {
                    System.getenv(SysEnvKey.LINKORA_HTTPS_PORT.name).toInt()
                } catch (_: Exception) {
                    54545
                },
                serverAuthToken = System.getenv(SysEnvKey.LINKORA_SERVER_AUTH_TOKEN.name),
                keyStorePassword = System.getenv(SysEnvKey.LINKORA_KEY_STORE_PASSWORD.name)
            )
        } else {
            createConfig(forceWrite = false)
            Files.readString(configFilePath).let {
                try {
                    json.decodeFromString<ServerConfig>(it).let {
                        val newKeyPassword = ServerConfig.generateAToken()
                        it.run {
                            if (keyStorePassword == null) {
                                createConfig(serverConfig = it.copy(keyStorePassword = newKeyPassword))
                            }
                            copy(
                                databaseUrl = "jdbc:" + it.databaseUrl,
                                keyStorePassword = keyStorePassword ?: newKeyPassword
                            )
                        }
                    }
                } catch (_: Exception) {
                    println("It seems you’ve manipulated `linkoraConfig.json` and messed things up a bit. No problemo, we’ll restart the configuration process to make sure things go smoothly.")
                    createConfig(forceWrite = true)
                    readConfig()
                }
            }
        }
    }

    fun exportSignedCertificates(keyStore: KeyStore) {
        val destinationPathForCer = serverJarDir.resolve("linkoraServerCert.cer")
        val destinationPathForPem = serverJarDir.resolve("linkoraServerCert.pem")
        val signedCertificate = keyStore.getCertificate(Constants.KEY_STORE_ALIAS)

        if (!destinationPathForCer.exists()) {
            FileOutputStream(destinationPathForCer.toFile()).use {
                it.write(signedCertificate.encoded)
            }
        }

        if (!destinationPathForPem.exists()) {
            FileWriter(destinationPathForPem.toFile()).use {
                it.write("-----BEGIN CERTIFICATE-----\n")
                it.write(Base64.getMimeEncoder().encodeToString(signedCertificate.encoded))
                it.write("\n-----END CERTIFICATE-----\n")
            }
        }
    }

    fun createOrLoadServerKeystore(serverConfig: ServerConfig, forceCreate: Boolean = false): KeyStore {
        require(serverConfig.keyStorePassword != null && serverConfig.keyStorePassword.isNotBlank()) {
            "keyStorePassword value must be set in ServerConfig."
        }
        val keyStore = buildKeyStore {
            this.certificate(
                alias = Constants.KEY_STORE_ALIAS, block = {
                    this.password = serverConfig.keyStorePassword
                    this.domains = listOf(serverConfig.serverHost)
                    this.ipAddresses = listOf(hostIp ?: localhost)
                    this.daysValid = 365
                    this.subject = X500Principal("CN=linkora-sync, OU=Linkora, O=Linkora, C=West Elizabeth")
                    this.keySizeInBits = 2048
                    this.hash = HashAlgorithm.SHA256
                    this.sign = SignatureAlgorithm.RSA
                })
        }
        return serverJarDir.resolve("linkoraServerCert.jks").run {
            if (forceCreate.not() && exists()) {
                FileInputStream(toFile()).use { keyStoreFile ->
                    KeyStore.getInstance(KeyStore.getDefaultType()).also {
                        println("Loading existing keystore...")
                        it.load(keyStoreFile, serverConfig.keyStorePassword.toCharArray())
                    }
                }
            } else {
                keyStore.also {
                    println("Creating new keystore...")
                    it.saveToFile(
                        output = toFile(), password = serverConfig.keyStorePassword
                    )
                }
            }
        }
    }
}

fun Application.module() {
    println("The server version is ${Constants.SERVER_VERSION}")
    configureDatabase()
    configureSerialization()
    install(CORS) {
        allowCredentials = true
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        anyHost()
    }
    val mdManagerRepo: MarkdownManagerRepo = MarkdownManagerRepoImpl()
    val serverConfig = ServerConfiguration.readConfig()
    configureRouting(serverConfig = serverConfig, markdownManagerRepo = mdManagerRepo)
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
    }
    configureEventsWebSocket()
    val serverConfiguredPage = "https://" + (hostIp?.hostAddress
        ?: localhost?.hostAddress) + ":" + serverConfig.httpsPort + "/" + Route.Sync.SERVER_IS_CONFIGURED.name
    if (useSysEnvValues().not() && Desktop.isDesktopSupported() && Desktop.getDesktop()
            .isSupported(Desktop.Action.BROWSE)
    ) {
        Desktop.getDesktop().browse(URI(serverConfiguredPage))
    }
}
