package com.sakethh.linkora.presentation.routing.http

import com.sakethh.linkora.Security
import com.sakethh.linkora.domain.DeleteMultipleItemsDTO
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.ArchiveMultipleItemsDTO
import com.sakethh.linkora.domain.dto.CopyItemsDTO
import com.sakethh.linkora.domain.dto.MoveItemsDTO
import com.sakethh.linkora.domain.repository.MultiActionRepo
import com.sakethh.linkora.utils.respondWithResult
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.multiActionRouting(multiActionRepo: MultiActionRepo) {
    routing {
        authenticate(Security.BEARER.name) {
            post<ArchiveMultipleItemsDTO>(Route.MultiAction.ARCHIVE_MULTIPLE_ITEMS.name) {
                respondWithResult(multiActionRepo.archiveMultipleItems(it))
            }
            post<DeleteMultipleItemsDTO>(Route.MultiAction.DELETE_MULTIPLE_ITEMS.name) {
                respondWithResult(multiActionRepo.deleteMultipleItems(it))
            }
            post<MoveItemsDTO>(Route.MultiAction.MOVE_EXISTING_ITEMS.name) {
                respondWithResult(multiActionRepo.moveMultipleItems(it))
            }
            post<CopyItemsDTO>(Route.MultiAction.COPY_EXISTING_ITEMS.name) {
                respondWithResult(multiActionRepo.copyMultipleItems(it))
            }
        }
    }
}