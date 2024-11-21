package com.sakethh.linkora

import io.ktor.server.application.*
import io.ktor.server.auth.*

fun Application.configureSecurity() {
    install(Authentication) {
        bearer(name = Security.BEARER.name) {
            authenticate { authToken ->
                if (authToken.token == ServerConfiguration.readConfig().serverAuthToken) {
                    UserIdPrincipal(name = "admin")
                } else {
                    null
                }
            }
        }
    }
}

enum class Security {
    BEARER
}
