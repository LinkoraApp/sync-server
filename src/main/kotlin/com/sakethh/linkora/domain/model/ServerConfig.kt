package com.sakethh.linkora.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.InetAddress

@Serializable
data class ServerConfig(
    val databaseUrl: String = "database_url",
    val databaseUser: String = "database_user",
    val databasePassword: String = "database_password",
    val hostAddress: String = InetAddress.getLocalHost().hostAddress,
    @SerialName("serverPort") val httpPort: Int = 45454,
    val httpsPort: Int = 54545,
    val serverAuthToken: String = "TOKEN",
    val keyStorePassword: String? = null
) {
    companion object {
        fun generateAToken(): String {
            return run {
                val chars = (0..9) + ('a'..'z') + ('A'..'Z') + listOf('!', '@', '#', '$', '%', '^', '&', '*')
                buildString {
                    repeat(45) {
                        append(chars.random())
                    }
                }
            }
        }
    }
}
