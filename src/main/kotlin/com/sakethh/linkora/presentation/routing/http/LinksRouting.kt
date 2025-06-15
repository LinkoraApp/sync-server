package com.sakethh.linkora.presentation.routing.http

import com.sakethh.linkora.authenticate
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.link.*
import com.sakethh.linkora.domain.repository.LinksRepo
import com.sakethh.linkora.utils.respondWithResult
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.linksRouting(linksRepo: LinksRepo) {
    routing {
        authenticate {
            post<AddLinkDTO>(Route.Link.CREATE_A_NEW_LINK.name) {
                respondWithResult(linksRepo.createANewLink(it))
            }

            post<IDBasedDTO>(Route.Link.DELETE_A_LINK.name) {
                respondWithResult(linksRepo.deleteALink(it))
            }

            post<UpdateLinkedFolderIDDto>(Route.Link.UPDATE_LINKED_FOLDER_ID.name) {
                respondWithResult(
                    linksRepo.updateLinkedFolderIdOfALink(it)
                )
            }

            post<UpdateTitleOfTheLinkDTO>(Route.Link.UPDATE_LINK_TITLE.name) {
                respondWithResult(
                    linksRepo.updateTitleOfTheLink(it)
                )
            }

            post<UpdateNoteOfALinkDTO>(Route.Link.UPDATE_LINK_NOTE.name) {
                respondWithResult(
                    linksRepo.updateNote(it)
                )
            }

            post<UpdateLinkUserAgentDTO>(Route.Link.UPDATE_USER_AGENT.name) {
                respondWithResult(
                    linksRepo.updateUserAgent(it)
                )
            }

            post<IDBasedDTO>(Route.Link.ARCHIVE_LINK.name) {
                respondWithResult(linksRepo.archiveALink(it))
            }

            post<IDBasedDTO>(Route.Link.UNARCHIVE_LINK.name) {
                respondWithResult(linksRepo.unArchiveALink(it))
            }

            post<IDBasedDTO>(Route.Link.MARK_AS_IMP.name) {
                respondWithResult(linksRepo.markALinkAsImp(it))
            }

            post<IDBasedDTO>(Route.Link.UNMARK_AS_IMP.name) {
                respondWithResult(linksRepo.markALinkAsNonImp(it))
            }

            post<LinkDTO>(Route.Link.UPDATE_LINK.name) {
                respondWithResult(linksRepo.updateLink(it))
            }

            post<DeleteDuplicateLinksDTO>(Route.Link.DELETE_DUPLICATE_LINKS.name) {
                respondWithResult(linksRepo.deleteDuplicateLinks(it))
            }
        }
    }
}