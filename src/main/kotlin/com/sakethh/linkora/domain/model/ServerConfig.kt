package com.sakethh.linkora.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ServerConfig(
    val databaseUrl: String = "database_url",
    val databaseUser: String = "database_user",
    val databasePassword: String = "database_password",
    val serverPort: Int = 45454,
    val serverAuthToken: String = "TOKEN",
)
