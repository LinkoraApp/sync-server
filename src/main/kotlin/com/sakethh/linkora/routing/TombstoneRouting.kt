package com.sakethh.linkora.routing

import com.sakethh.linkora.Security
import com.sakethh.linkora.domain.repository.TombstoneRepo
import com.sakethh.linkora.domain.routes.AppRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.tombStoneRouting(tombstoneRepo: TombstoneRepo) {
    routing {
        authenticate(Security.BEARER.name) {
            get(AppRoute.TOMBSTONES.name) {
                val timestamp: Long
                try {
                    val paramTimeStampValue = this.call.parameters["timestamp"]?.toLong()
                    if (paramTimeStampValue != null) {
                        timestamp = paramTimeStampValue
                    } else {
                        throw IllegalArgumentException("Expected a valid timestamp value, but received null.")
                    }
                } catch (e: Exception) {
                    call.respond(message = e.message.toString(), status = HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    call.respond(tombstoneRepo.getTombstonesAfter(timestamp))
                } catch (e: Exception) {
                    call.respond(e.message.toString())
                }
            }
        }
    }
}