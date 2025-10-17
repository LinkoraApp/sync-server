package com.sakethh.linkora.presentation.routing.http

import com.sakethh.linkora.authenticate
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.DeleteEverythingDTO
import com.sakethh.linkora.domain.repository.SyncRepo
import com.sakethh.linkora.utils.respondWithResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.syncRouting(syncRepo: SyncRepo) {
    authenticate {
        get(Route.Sync.GET_TOMBSTONES.name) {
            val eventTimestamp = getTimeStampFromParam() ?: return@get
            try {
                call.respond(syncRepo.getTombstonesAfter(eventTimestamp))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(e.message.toString())
            }
        }

        get(Route.Sync.GET_UPDATES.name) {
            val eventTimestamp = getTimeStampFromParam() ?: return@get
            try {
                call.respond(syncRepo.getUpdatesAfter(eventTimestamp))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(e.message.toString())
            }
        }

        post<DeleteEverythingDTO>(Route.Sync.DELETE_EVERYTHING.name) {
            respondWithResult(syncRepo.deleteEverything(it))
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