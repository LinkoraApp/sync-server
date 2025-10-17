package com.sakethh.linkora.presentation.routing.http

import com.sakethh.linkora.authenticate
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.tag.CreateTagDTO
import com.sakethh.linkora.domain.dto.tag.RenameTagDTO
import com.sakethh.linkora.domain.repository.TagsRepo
import com.sakethh.linkora.utils.respondWithResult
import io.ktor.server.routing.*

fun Routing.tagsRouting(tagsRepo: TagsRepo) {
    authenticate {
        post<CreateTagDTO>(path = Route.Tag.CREATE_TAG.name) {
            respondWithResult(tagsRepo.createATag(it))
        }
        post<RenameTagDTO>(path = Route.Tag.RENAME_TAG.name) {
            respondWithResult(tagsRepo.renameATag(it))
        }
        post<IDBasedDTO>(path = Route.Tag.DELETE_TAG.name) {
            respondWithResult(tagsRepo.deleteATag(it))
        }
    }
}