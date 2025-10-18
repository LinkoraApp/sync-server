package com.sakethh.linkora.domain.dto

import com.sakethh.linkora.domain.model.*
import kotlinx.serialization.Serializable

@Serializable
data class AllTablesDTO(
    val links: List<Link>,
    val folders: List<Folder>,
    val panels: List<Panel>,
    val panelFolders: List<PanelFolder>,
    val tags: List<Tag>,
    val linkTags: List<LinkTag>
)
