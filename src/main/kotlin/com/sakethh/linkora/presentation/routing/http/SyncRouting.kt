package com.sakethh.linkora.presentation.routing.http

import com.sakethh.linkora.Security
import com.sakethh.linkora.domain.repository.SyncRepo
import com.sakethh.linkora.domain.routes.SyncRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.syncRouting(syncRepo: SyncRepo) {
    routing {
        authenticate(Security.BEARER.name) {
            get(SyncRoute.GET_TOMBSTONES.name) {
                val eventTimestamp = getTimeStampFromParam() ?: return@get
                try {
                    call.respond(syncRepo.getTombstonesAfter(eventTimestamp))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(e.message.toString())
                }
            }

            get(SyncRoute.GET_UPDATES.name) {
                val eventTimestamp = getTimeStampFromParam() ?: return@get
                try {
                    call.respond(syncRepo.getUpdatesAfter(eventTimestamp))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(e.message.toString())
                }
            }

            get(SyncRoute.DELETE_EVERYTHING.name) {
                syncRepo.deleteEverything().onSuccess {
                    call.respond(status = HttpStatusCode.OK, message = HttpStatusCode.OK.description)
                }.onFailure {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        message = HttpStatusCode.InternalServerError.description
                    )
                    it.printStackTrace()
                }
            }
        }
    }
}

private suspend fun RoutingContext.getTimeStampFromParam(): Long? {
    return try {
        this.call.parameters["eventTimestamp"]?.toLong()
            ?: throw IllegalArgumentException("Expected a valid eventTimestamp value, but received null.")
    } catch (e: Exception) {
        e.printStackTrace()
        call.respond(message = e.message.toString(), status = HttpStatusCode.BadRequest)
        null
    }
}