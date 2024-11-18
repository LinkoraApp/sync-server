package com.sakethh.linkora

import com.sakethh.linkora.data.repository.FoldersImplementation
import com.sakethh.linkora.domain.repository.FoldersRepository
import com.sakethh.linkora.routing.foldersRouting
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
    val foldersRepository: FoldersRepository = FoldersImplementation()
    foldersRouting(foldersRepository)
}
