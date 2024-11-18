package com.sakethh.linkora.domain.mapper

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.dto.nonSavedLinksAndNonFolderLinks.NonSavedAndNonFolderLinkDTO
import com.sakethh.linkora.domain.tables.ArchiveLinksTable
import com.sakethh.linkora.domain.tables.HistoryLinksTable
import com.sakethh.linkora.domain.tables.ImportantLinksTable
import org.jetbrains.exposed.sql.Query

class NonSavedAndNonFolderLinksMapper {
    fun toDto(linkType: LinkType, query: Query): List<NonSavedAndNonFolderLinkDTO> {
        return query.map { resultRow ->
            NonSavedAndNonFolderLinkDTO(
                linkType = linkType,
                id = when (linkType) {
                    LinkType.HISTORY_LINK -> resultRow[HistoryLinksTable.id]
                    LinkType.IMPORTANT_LINK -> resultRow[ImportantLinksTable.id]
                    else /*  else = LinkType.ARCHIVE_LINK */ -> resultRow[ArchiveLinksTable.id]
                }.value,
                linkTitle = when (linkType) {
                    LinkType.HISTORY_LINK -> resultRow[HistoryLinksTable.linkTitle]
                    LinkType.IMPORTANT_LINK -> resultRow[ImportantLinksTable.linkTitle]
                    else /*  else = LinkType.ARCHIVE_LINK */ -> resultRow[ArchiveLinksTable.linkTitle]
                },
                webURL = when (linkType) {
                    LinkType.HISTORY_LINK -> resultRow[HistoryLinksTable.webURL]
                    LinkType.IMPORTANT_LINK -> resultRow[ImportantLinksTable.webURL]
                    else /*  else = LinkType.ARCHIVE_LINK */ -> resultRow[ArchiveLinksTable.webURL]
                },
                baseURL = when (linkType) {
                    LinkType.HISTORY_LINK -> resultRow[HistoryLinksTable.baseURL]
                    LinkType.IMPORTANT_LINK -> resultRow[ImportantLinksTable.baseURL]
                    else /*  else = LinkType.ARCHIVE_LINK */ -> resultRow[ArchiveLinksTable.baseURL]
                },
                imgURL = when (linkType) {
                    LinkType.HISTORY_LINK -> resultRow[HistoryLinksTable.imgURL]
                    LinkType.IMPORTANT_LINK -> resultRow[ImportantLinksTable.imgURL]
                    else /*  else = LinkType.ARCHIVE_LINK */ -> resultRow[ArchiveLinksTable.imgURL]
                },
                infoForSaving = when (linkType) {
                    LinkType.HISTORY_LINK -> resultRow[HistoryLinksTable.infoForSaving]
                    LinkType.IMPORTANT_LINK -> resultRow[ImportantLinksTable.infoForSaving]
                    else /*  else = LinkType.ARCHIVE_LINK */ -> resultRow[ArchiveLinksTable.infoForSaving]
                },
                userAgent = when (linkType) {
                    LinkType.HISTORY_LINK -> resultRow[HistoryLinksTable.userAgent]
                    LinkType.IMPORTANT_LINK -> resultRow[ImportantLinksTable.userAgent]
                    else /*  else = LinkType.ARCHIVE_LINK */ -> resultRow[ArchiveLinksTable.userAgent]
                }
            )
        }
    }
}