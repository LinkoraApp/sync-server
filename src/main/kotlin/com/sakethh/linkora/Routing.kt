package com.sakethh.linkora

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(status = HttpStatusCode.OK, message = HttpStatusCode.OK.description)
        }
    }
}
