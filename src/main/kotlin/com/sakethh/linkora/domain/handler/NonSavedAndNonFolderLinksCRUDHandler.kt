package com.sakethh.linkora.domain.handler

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.dto.nonSavedLinksAndNonFolderLinks.NonSavedAndNonFolderLinkDTO
import com.sakethh.linkora.domain.tables.ArchiveLinksTable
import com.sakethh.linkora.domain.tables.HistoryLinksTable
import com.sakethh.linkora.domain.tables.ImportantLinksTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.insertAndGetId

class NonSavedAndNonFolderLinksCRUDHandler {
    fun insertAndGetId(
        longIdTable: LongIdTable,
        linkType: LinkType,
        nonSavedAndNonFolderLinkDTO: NonSavedAndNonFolderLinkDTO
    ): EntityID<Long> {
        return longIdTable.insertAndGetId { link ->
            link[when (linkType) {
                LinkType.HISTORY_LINK -> HistoryLinksTable.linkTitle
                LinkType.IMPORTANT_LINK -> ImportantLinksTable.linkTitle
                else /*  else = LinkType.ARCHIVE_LINK */ -> ArchiveLinksTable.linkTitle
            }] = nonSavedAndNonFolderLinkDTO.linkTitle

            link[when (linkType) {
                LinkType.HISTORY_LINK -> HistoryLinksTable.webURL
                LinkType.IMPORTANT_LINK -> ImportantLinksTable.webURL
                else /*  else = LinkType.ARCHIVE_LINK */ -> ArchiveLinksTable.webURL
            }] = nonSavedAndNonFolderLinkDTO.webURL

            link[when (linkType) {
                LinkType.HISTORY_LINK -> HistoryLinksTable.baseURL
                LinkType.IMPORTANT_LINK -> ImportantLinksTable.baseURL
                else /*  else = LinkType.ARCHIVE_LINK */ -> ArchiveLinksTable.baseURL
            }] = nonSavedAndNonFolderLinkDTO.baseURL

            link[when (linkType) {
                LinkType.HISTORY_LINK -> HistoryLinksTable.imgURL
                LinkType.IMPORTANT_LINK -> ImportantLinksTable.imgURL
                else /*  else = LinkType.ARCHIVE_LINK */ -> ArchiveLinksTable.imgURL
            }] = nonSavedAndNonFolderLinkDTO.imgURL

            link[when (linkType) {
                LinkType.HISTORY_LINK -> HistoryLinksTable.infoForSaving
                LinkType.IMPORTANT_LINK -> ImportantLinksTable.infoForSaving
                else /*  else = LinkType.ARCHIVE_LINK */ -> ArchiveLinksTable.infoForSaving
            }] = nonSavedAndNonFolderLinkDTO.infoForSaving

            link[when (linkType) {
                LinkType.HISTORY_LINK -> HistoryLinksTable.userAgent
                LinkType.IMPORTANT_LINK -> ImportantLinksTable.userAgent
                else /*  else = LinkType.ARCHIVE_LINK */ -> ArchiveLinksTable.userAgent
            }] = nonSavedAndNonFolderLinkDTO.userAgent
        }
    }
}