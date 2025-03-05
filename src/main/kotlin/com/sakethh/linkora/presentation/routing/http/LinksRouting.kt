package com.sakethh.linkora.presentation.routing.http

import com.sakethh.linkora.Security
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.link.*
import com.sakethh.linkora.domain.repository.LinksRepository
import com.sakethh.linkora.utils.respondWithResult
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.linksRouting(linksRepository: LinksRepository) {
    routing {
        authenticate(Security.BEARER.name) {
            post<AddLinkDTO>(Route.Link.CREATE_A_NEW_LINK.name) {
                respondWithResult(linksRepository.createANewLink(it))
            }

            post<IDBasedDTO>(Route.Link.DELETE_A_LINK.name) {
                respondWithResult(linksRepository.deleteALink(it))
            }

            post<UpdateLinkedFolderIDDto>(Route.Link.UPDATE_LINKED_FOLDER_ID.name) {
                respondWithResult(
                    linksRepository.updateLinkedFolderIdOfALink(it)
                )
            }

            post<UpdateTitleOfTheLinkDTO>(Route.Link.UPDATE_LINK_TITLE.name) {
                respondWithResult(
                    linksRepository.updateTitleOfTheLink(it)
                )
            }

            post<UpdateNoteOfALinkDTO>(Route.Link.UPDATE_LINK_NOTE.name) {
                respondWithResult(
                    linksRepository.updateNote(it)
                )
            }

            post<UpdateLinkUserAgentDTO>(Route.Link.UPDATE_USER_AGENT.name) {
                respondWithResult(
                    linksRepository.updateUserAgent(it)
                )
            }

            post<IDBasedDTO>(Route.Link.ARCHIVE_LINK.name) {
                respondWithResult(linksRepository.archiveALink(it))
            }

            post<IDBasedDTO>(Route.Link.UNARCHIVE_LINK.name) {
                respondWithResult(linksRepository.unArchiveALink(it))
            }

            post<IDBasedDTO>(Route.Link.MARK_AS_IMP.name) {
                respondWithResult(linksRepository.markALinkAsImp(it))
            }

            post<IDBasedDTO>(Route.Link.UNMARK_AS_IMP.name) {
                respondWithResult(linksRepository.markALinkAsNonImp(it))
            }

            post<LinkDTO>(Route.Link.UPDATE_LINK.name) {
                respondWithResult(linksRepository.updateLink(it))
            }

            post<DeleteDuplicateLinksDTO>(Route.Link.DELETE_DUPLICATE_LINKS.name) {
                respondWithResult(linksRepository.deleteDuplicateLinks(it))
            }
        }
    }
}