package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.dto.nonSavedLinksAndNonFolderLinks.NonSavedAndNonFolderLinkDTO
import com.sakethh.linkora.domain.handler.NonSavedAndNonFolderLinksCRUDHandler
import com.sakethh.linkora.domain.mapper.NonSavedAndNonFolderLinksMapper
import com.sakethh.linkora.domain.repository.Message
import com.sakethh.linkora.domain.repository.NonSavedAndNonFolderLinksRepository
import com.sakethh.linkora.domain.tables.ArchiveLinksTable
import com.sakethh.linkora.domain.tables.HistoryLinksTable
import com.sakethh.linkora.domain.tables.ImportantLinksTable
import com.sakethh.linkora.utils.RequestResultState
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class NonSavedAndNonFolderLinksImplementation(
    private val nonSavedAndNonFolderLinksCRUDHandler: NonSavedAndNonFolderLinksCRUDHandler = NonSavedAndNonFolderLinksCRUDHandler(),
    private val nonSavedAndNonFolderLinksMapper: NonSavedAndNonFolderLinksMapper = NonSavedAndNonFolderLinksMapper()
) : NonSavedAndNonFolderLinksRepository {
    override suspend fun createANewLink(
        linkType: LinkType,
        nonSavedAndNonFolderLinkDTO: NonSavedAndNonFolderLinkDTO
    ): RequestResultState<Message> {
        return try {
            transaction {
                when (linkType.name) {
                    LinkType.HISTORY_LINK.name -> HistoryLinksTable
                    LinkType.IMPORTANT_LINK.name -> ImportantLinksTable
                    LinkType.ARCHIVE_LINK.name -> ArchiveLinksTable
                    else -> throw InvalidLinkTypeException(linkType = linkType)
                }.insertAndGetId { _ ->
                    nonSavedAndNonFolderLinksCRUDHandler.insertAndGetId(
                        longIdTable = this,
                        linkType = linkType,
                        nonSavedAndNonFolderLinkDTO = nonSavedAndNonFolderLinkDTO
                    )
                }
            }.let {
                RequestResultState.Success("A link has been created for ${linkType.name} with id = ${it.value}.")
            }
        } catch (e: Exception) {
            RequestResultState.Failure(exception = e)
        }
    }

    override suspend fun deleteANewLink(linkType: LinkType, linkId: Long): RequestResultState<Message> {
        return try {
            transaction {
                when (linkType.name) {
                    LinkType.HISTORY_LINK.name -> HistoryLinksTable.deleteWhere {
                        id.eq(linkId)
                    }

                    LinkType.IMPORTANT_LINK.name -> ImportantLinksTable.deleteWhere {
                        id.eq(linkId)
                    }

                    LinkType.ARCHIVE_LINK.name -> ArchiveLinksTable.deleteWhere {
                        id.eq(linkId)
                    }

                    else -> throw InvalidLinkTypeException(linkType = linkType)
                }
            }.let {
                RequestResultState.Success("Count of deleted rows = ${it}.")
            }
        } catch (e: Exception) {
            return RequestResultState.Failure(exception = e)
        }
    }

    override suspend fun getAllLinks(linkType: LinkType): RequestResultState<List<NonSavedAndNonFolderLinkDTO>> {
        return try {
            transaction {
                nonSavedAndNonFolderLinksMapper.toDto(
                    linkType = linkType, query = when (linkType.name) {
                        LinkType.HISTORY_LINK.name -> HistoryLinksTable.selectAll()
                        LinkType.IMPORTANT_LINK.name -> ImportantLinksTable.selectAll()
                        LinkType.ARCHIVE_LINK.name -> ArchiveLinksTable.selectAll()
                        else -> throw InvalidLinkTypeException(linkType = linkType)
                    }
                )
            }.let {
                RequestResultState.Success(it)
            }
        } catch (e: Exception) {
            RequestResultState.Failure(exception = e)
        }
    }

    override suspend fun updateLinkTitle(
        linkType: LinkType,
        linkId: Long,
        newTitle: String
    ): RequestResultState<Message> {
        return try {
            transaction {
                when (linkType.name) {
                    LinkType.HISTORY_LINK.name -> HistoryLinksTable.update(where = {
                        HistoryLinksTable.id eq linkId
                    }) {
                        it[linkTitle] = newTitle
                    }

                    LinkType.IMPORTANT_LINK.name -> ImportantLinksTable.update(where = {
                        ImportantLinksTable.id eq linkId
                    }) {
                        it[linkTitle] = newTitle
                    }

                    LinkType.ARCHIVE_LINK.name -> ArchiveLinksTable.update(where = {
                        ArchiveLinksTable.id eq linkId
                    }) {
                        it[linkTitle] = newTitle
                    }

                    else -> throw InvalidLinkTypeException(linkType = linkType)
                }
            }.let {
                RequestResultState.Success("Count of updated rows = ${it}.")
            }
        } catch (e: Exception) {
            return RequestResultState.Failure(exception = e)
        }
    }

    override suspend fun updateLinkNote(
        linkType: LinkType,
        linkId: Long,
        newNote: String
    ): RequestResultState<Message> {
        return try {
            transaction {
                when (linkType.name) {
                    LinkType.HISTORY_LINK.name -> HistoryLinksTable.update(where = {
                        HistoryLinksTable.id eq linkId
                    }) {
                        it[infoForSaving] = newNote
                    }

                    LinkType.IMPORTANT_LINK.name -> ImportantLinksTable.update(where = {
                        ImportantLinksTable.id eq linkId
                    }) {
                        it[infoForSaving] = newNote
                    }

                    LinkType.ARCHIVE_LINK.name -> ArchiveLinksTable.update(where = {
                        ArchiveLinksTable.id eq linkId
                    }) {
                        it[infoForSaving] = newNote
                    }

                    else -> throw InvalidLinkTypeException(linkType = linkType)
                }
            }.let {
                RequestResultState.Success("Count of updated rows = ${it}.")
            }
        } catch (e: Exception) {
            return RequestResultState.Failure(exception = e)
        }
    }

    override suspend fun deleteLinkNote(linkType: LinkType, linkId: Long): RequestResultState<Message> {
        return try {
            transaction {
                when (linkType.name) {
                    LinkType.HISTORY_LINK.name -> HistoryLinksTable.deleteWhere {
                        id.eq(linkId)
                    }

                    LinkType.IMPORTANT_LINK.name -> ImportantLinksTable.deleteWhere {
                        id.eq(linkId)
                    }

                    LinkType.ARCHIVE_LINK.name -> ArchiveLinksTable.deleteWhere {
                        id.eq(linkId)
                    }

                    else -> throw InvalidLinkTypeException(linkType = linkType)
                }
            }.let {
                RequestResultState.Success("Count of deleted rows = ${it}.")
            }
        } catch (e: Exception) {
            return RequestResultState.Failure(exception = e)
        }
    }
}

class InvalidLinkTypeException(
    val linkType: LinkType,
    message: String = "Invalid LinkType: ${linkType.name}. Valid types are: HISTORY_LINK, IMPORTANT_LINK, ARCHIVE_LINK."
) : Exception(message)