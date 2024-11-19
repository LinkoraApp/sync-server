package com.sakethh.linkora.domain.model

import kotlinx.serialization.Serializable
import java.net.InetAddress

@Serializable
data class ServerConfig(
    val databaseUrl: String = "database_url",
    val databaseUser: String = "database_user",
    val databasePassword: String = "database_password",
    val hostAddress: String = InetAddress.getLocalHost().hostAddress,
    val serverPort: Int = 45454
)
