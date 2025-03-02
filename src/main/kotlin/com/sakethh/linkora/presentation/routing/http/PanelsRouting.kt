package com.sakethh.linkora.presentation.routing.http

import com.sakethh.linkora.Security
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.panel.AddANewPanelDTO
import com.sakethh.linkora.domain.dto.panel.AddANewPanelFolderDTO
import com.sakethh.linkora.domain.dto.panel.DeleteAFolderFromAPanelDTO
import com.sakethh.linkora.domain.dto.panel.UpdatePanelNameDTO
import com.sakethh.linkora.domain.repository.PanelsRepository
import com.sakethh.linkora.utils.respondWithResult
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import com.sakethh.linkora.domain.Route

fun Application.panelsRouting(panelsRepository: PanelsRepository) {
    routing {
        authenticate(Security.BEARER.name) {
            post<AddANewPanelDTO>(Route.Panel.ADD_A_NEW_PANEL.name) {
                respondWithResult(panelsRepository.addANewPanel(it))
            }

            post<AddANewPanelFolderDTO>(Route.Panel.ADD_A_NEW_FOLDER_IN_A_PANEL.name) {
                respondWithResult(panelsRepository.addANewFolderInAPanel(it))
            }

            post<IDBasedDTO>(Route.Panel.DELETE_A_PANEL.name) {
                respondWithResult(panelsRepository.deleteAPanel(it))
            }

            post<UpdatePanelNameDTO>(Route.Panel.UPDATE_A_PANEL_NAME.name) {
                respondWithResult(panelsRepository.updateAPanelName(it))
            }

            post<IDBasedDTO>(Route.Panel.DELETE_A_FOLDER_FROM_ALL_PANELS.name) {
                respondWithResult(panelsRepository.deleteAFolderFromAllPanels(it))
            }

            post<DeleteAFolderFromAPanelDTO>(Route.Panel.DELETE_A_FOLDER_FROM_A_PANEL.name) {
                respondWithResult(panelsRepository.deleteAFolderFromAPanel(it))
            }

            post<IDBasedDTO>(Route.Panel.DELETE_ALL_FOLDERS_FROM_A_PANEL.name) {
                respondWithResult(panelsRepository.deleteAllFoldersFromAPanel(it))
            }
        }
    }
}