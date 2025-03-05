package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.DeleteMultipleItemsDTO
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.ArchiveMultipleItemsDTO
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.TimeStampBasedResponse
import com.sakethh.linkora.domain.model.WebSocketEvent
import com.sakethh.linkora.domain.repository.FoldersRepository
import com.sakethh.linkora.domain.repository.LinksRepository
import com.sakethh.linkora.domain.repository.MultiActionRepo
import com.sakethh.linkora.domain.tables.FoldersTable
import com.sakethh.linkora.domain.tables.LinksTable
import com.sakethh.linkora.domain.tables.helper.TombStoneHelper
import com.sakethh.linkora.utils.checkForLWWConflictAndThrow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

class MultiActionRepoImpl(
    private val foldersRepository: FoldersRepository
) : MultiActionRepo {
    override suspend fun archiveMultipleItems(archiveMultipleItemsDTO: ArchiveMultipleItemsDTO): Result<TimeStampBasedResponse> {
        return try {
            if (archiveMultipleItemsDTO.linkIds.isNotEmpty()) {
                LinksTable.checkForLWWConflictAndThrow(
                    id = archiveMultipleItemsDTO.linkIds.last(),
                    timeStamp = archiveMultipleItemsDTO.eventTimestamp,
                    lastModifiedColumn = LinksTable.lastModified
                )
            }
            if (archiveMultipleItemsDTO.folderIds.isNotEmpty()) {
                FoldersTable.checkForLWWConflictAndThrow(
                    id = archiveMultipleItemsDTO.folderIds.last(),
                    timeStamp = archiveMultipleItemsDTO.eventTimestamp,
                    lastModifiedColumn = FoldersTable.lastModified
                )
            }
            val eventTimestamp = Instant.now().epochSecond
            var updatedRowsCount = 0
            coroutineScope {
                awaitAll(async {
                    transaction {
                        updatedRowsCount += LinksTable.update(where = {
                            LinksTable.id.inList(archiveMultipleItemsDTO.linkIds)
                        }) {
                            it[linkType] = LinkType.ARCHIVE_LINK.name
                        }
                    }
                }, async {
                    transaction {
                        updatedRowsCount += FoldersTable.update(where = {
                            FoldersTable.id.inList(archiveMultipleItemsDTO.folderIds)
                        }) {
                            it[isFolderArchived] = true
                        }
                    }
                })
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    eventTimestamp = eventTimestamp, message = "Archived $updatedRowsCount items."
                ),
                webSocketEvent = WebSocketEvent(
                    operation = Route.MultiAction.ARCHIVE_MULTIPLE_ITEMS.name,
                    payload = Json.encodeToJsonElement(archiveMultipleItemsDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteMultipleItems(deleteMultipleItemsDTO: DeleteMultipleItemsDTO): Result<TimeStampBasedResponse> {
        val eventTimestamp = Instant.now().epochSecond
        return try {
            deleteMultipleItemsDTO.folderIds.forEach {
                foldersRepository.deleteFolder(
                    IDBasedDTO(
                        id = it,
                        correlation = deleteMultipleItemsDTO.correlation,
                        eventTimestamp = deleteMultipleItemsDTO.eventTimestamp
                    )
                )
            }
            transaction {
                TombStoneHelper.insert(
                    payload = Json.encodeToString(deleteMultipleItemsDTO.copy(eventTimestamp = eventTimestamp)),
                    operation = Route.MultiAction.DELETE_MULTIPLE_ITEMS.name,
                    deletedAt = eventTimestamp
                )

                LinksTable.deleteWhere {
                    LinksTable.id.inList(deleteMultipleItemsDTO.linkIds)
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    eventTimestamp = eventTimestamp, message = "Deleted."
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.MultiAction.DELETE_MULTIPLE_ITEMS.name,
                    payload = Json.encodeToJsonElement(deleteMultipleItemsDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}