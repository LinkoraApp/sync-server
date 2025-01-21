package com.sakethh.linkora.routing

import com.sakethh.linkora.Security
import com.sakethh.linkora.domain.Link
import com.sakethh.linkora.domain.dto.link.*
import com.sakethh.linkora.domain.repository.LinksRepository
import com.sakethh.linkora.domain.routes.LinkRoute
import com.sakethh.linkora.utils.respondWithResult
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.linksRouting(linksRepository: LinksRepository) {
    routing {
        authenticate(Security.BEARER.name) {
            post<AddLinkDTO>(LinkRoute.CREATE_A_NEW_LINK.name) {
                respondWithResult(linksRepository.createANewLink(it))
            }

            post<Long>(LinkRoute.DELETE_A_LINK.name) {
                respondWithResult(linksRepository.deleteALink(it))
            }

            post<UpdateLinkedFolderIDDto>(LinkRoute.UPDATE_LINKED_FOLDER_ID.name) {
                respondWithResult(
                    linksRepository.updateLinkedFolderIdOfALink(it)
                )
            }

            post<UpdateTitleOfTheLinkDTO>(LinkRoute.UPDATE_LINK_TITLE.name) {
                respondWithResult(
                    linksRepository.updateTitleOfTheLink(it)
                )
            }

            post<UpdateNoteOfALinkDTO>(LinkRoute.UPDATE_LINK_NOTE.name) {
                respondWithResult(
                    linksRepository.updateNote(it)
                )
            }

            post<UpdateLinkUserAgentDTO>(LinkRoute.UPDATE_USER_AGENT.name) {
                respondWithResult(
                    linksRepository.updateUserAgent(it)
                )
            }

            post<Long>(LinkRoute.ARCHIVE_LINK.name) {
                respondWithResult(linksRepository.archiveALink(linkId = it))
            }

            post<Long>(LinkRoute.UNARCHIVE_LINK.name) {
                respondWithResult(linksRepository.unArchiveALink(linkId = it))
            }

            post<Long>(LinkRoute.MARK_AS_IMP.name) {
                respondWithResult(linksRepository.markALinkAsImp(linkId = it))
            }

            post<Long>(LinkRoute.UNMARK_AS_IMP.name) {
                respondWithResult(linksRepository.markALinkAsNonImp(linkId = it))
            }

            post<Link>(LinkRoute.UPDATE_LINK.name) {
                respondWithResult(linksRepository.updateLink(link = it))
            }
        }
    }
}