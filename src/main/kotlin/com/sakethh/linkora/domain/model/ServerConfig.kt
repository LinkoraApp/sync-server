package com.sakethh.linkora.domain.model

import com.sakethh.linkora.hostIp
import com.sakethh.linkora.localhost
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerConfig(
    val databaseUrl: String = "database_url",
    val databaseUser: String = "database_user",
    val databasePassword: String = "database_password",
    @SerialName("hostAddress") val serverHost: String = try {
        require(hostIp != null)
        hostIp.hostAddress
    } catch (e: Exception) {
        e.printStackTrace()
        localhost.hostAddress
    },
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