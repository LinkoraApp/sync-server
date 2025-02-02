package com.sakethh.linkora.domain.dto

import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.Link
import com.sakethh.linkora.domain.model.Panel
import com.sakethh.linkora.domain.model.PanelFolder
import kotlinx.serialization.Serializable

@Serializable
data class AllTablesDTO(
    val links: List<Link>,
    val folders: List<Folder>,
    val panels: List<Panel>,
    val panelFolders: List<PanelFolder>
)
