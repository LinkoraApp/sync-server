package com.sakethh.linkora.routing

import com.sakethh.linkora.Security
import com.sakethh.linkora.domain.LinkType
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
            post<LinkDTO>(LinkRoute.CREATE_A_NEW_LINK.name) {
                respondWithResult(linksRepository.createANewLink(it))
            }
        }

        authenticate(Security.BEARER.name) {
            post<DeleteALinkDTO>(LinkRoute.DELETE_A_LINK.name) {
                respondWithResult(linksRepository.deleteALink(it))
            }
        }

        authenticate(Security.BEARER.name) {
            post<UpdateLinkedFolderIDDto>(LinkRoute.UPDATE_LINKED_FOLDER_ID.name) {
                respondWithResult(
                    linksRepository.updateLinkedFolderIdOfALink(it)
                )
            }
        }

        authenticate(Security.BEARER.name) {
            post<UpdateTitleOfTheLinkDTO>(LinkRoute.UPDATE_LINK_TITLE.name) {
                respondWithResult(
                    linksRepository.updateTitleOfTheLink(it)
                )
            }
        }

        authenticate(Security.BEARER.name) {
            post<UpdateNoteOfALinkDTO>(LinkRoute.UPDATE_LINK_NOTE.name) {
                respondWithResult(
                    linksRepository.updateNote(it)
                )
            }
        }

        authenticate(Security.BEARER.name) {
            post<UpdateLinkUserAgentDTO>(LinkRoute.UPDATE_USER_AGENT.name) {
                respondWithResult(
                    linksRepository.updateUserAgent(it)
                )
            }
        }

        authenticate(Security.BEARER.name) {
            post<LinkType>(LinkRoute.GET_LINKS.name) {
                respondWithResult(linksRepository.getLinks(linkType = it))
            }
        }

        authenticate(Security.BEARER.name) {
            post<Long>(LinkRoute.GET_LINKS_FROM_A_FOLDER.name) {
                respondWithResult(linksRepository.getLinksFromAFolder(it))
            }
        }
    }
}