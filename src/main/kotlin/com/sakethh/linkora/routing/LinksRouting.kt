package com.sakethh.linkora.routing

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.dto.link.*
import com.sakethh.linkora.domain.repository.LinksRepository
import com.sakethh.linkora.domain.routes.LinkRoute
import com.sakethh.linkora.utils.respondWithResult
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.linksRouting(linksRepository: LinksRepository) {
    routing {
        post<LinkDTO>(LinkRoute.CREATE_A_NEW_LINK.name) {
            respondWithResult(linksRepository.createANewLink(it))
        }

        post<DeleteALinkDTO>(LinkRoute.DELETE_A_LINK.name) {
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

        post<LinkType>(LinkRoute.GET_LINKS.name) {
            respondWithResult(linksRepository.getLinks(linkType = it))
        }

        post<Long>(LinkRoute.GET_LINKS_FROM_A_FOLDER.name) {
            respondWithResult(linksRepository.getLinksFromAFolder(it))
        }
    }
}