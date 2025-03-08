package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.DeleteMultipleItemsDTO
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.*
import com.sakethh.linkora.domain.model.WebSocketEvent
import com.sakethh.linkora.domain.repository.FoldersRepo
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
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

class MultiActionRepoImpl(
    private val foldersRepo: FoldersRepo
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
                foldersRepo.deleteFolder(
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

    override suspend fun moveMultipleItems(moveItemsDTO: MoveItemsDTO): Result<TimeStampBasedResponse> {
        return try {
            if (moveItemsDTO.folderIds.isNotEmpty()) {
                FoldersTable.checkForLWWConflictAndThrow(
                    id = moveItemsDTO.folderIds.last(),
                    timeStamp = moveItemsDTO.eventTimestamp,
                    lastModifiedColumn = FoldersTable.lastModified
                )
            }
            if (moveItemsDTO.linkIds.isNotEmpty()) {
                LinksTable.checkForLWWConflictAndThrow(
                    id = moveItemsDTO.linkIds.last(),
                    timeStamp = moveItemsDTO.eventTimestamp,
                    lastModifiedColumn = LinksTable.lastModified
                )
            }
            val eventTimestamp = Instant.now().epochSecond
            var rowsUpdated = 0
            transaction {
                rowsUpdated += LinksTable.update(where = {
                    LinksTable.id.inList(moveItemsDTO.linkIds)
                }) {
                    it[lastModified] = eventTimestamp
                    it[idOfLinkedFolder] = moveItemsDTO.newParentFolderId
                    it[linkType] = moveItemsDTO.linkType.name
                }
                rowsUpdated += FoldersTable.update(where = { FoldersTable.id.inList(moveItemsDTO.folderIds) }) {
                    it[lastModified] = eventTimestamp
                    it[parentFolderID] = moveItemsDTO.newParentFolderId
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    message = "Number of rows affected by the update = $rowsUpdated", eventTimestamp = eventTimestamp
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.MultiAction.MOVE_EXISTING_ITEMS.name,
                    payload = Json.encodeToJsonElement(
                        moveItemsDTO.copy(
                            eventTimestamp = eventTimestamp
                        )
                    ),
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun copyMultipleItems(copyItemsDTO: CopyItemsDTO): Result<CopyItemsResponseDTO> {
        return try {
            val eventTimestamp = Instant.now().epochSecond
            lateinit var linkIds: List<Long>

            // copy the links based on `copyItemsDTO.linkIds`
            transaction {
                this.exec(
                    """
                   INSERT INTO links_table (last_modified,link_type,link_title,url,base_url,img_url,note,id_of_linked_folder,user_agent,media_type,marked_as_important)
                   SELECT $eventTimestamp AS last_modified,'${copyItemsDTO.linkType.name}' AS link_type,link_title,url,base_url,img_url,note,${copyItemsDTO.newParentFolderId} AS id_of_linked_folder,user_agent,media_type,marked_as_important FROM links_table
                   WHERE id IN (${copyItemsDTO.linkIds.values.joinToString(separator = ",")});
               """.trimIndent()
                )
                linkIds = LinksTable.select(LinksTable.id).where {
                    LinksTable.lastModified.eq(eventTimestamp) and LinksTable.linkType.eq(copyItemsDTO.linkType.name) and LinksTable.idOfLinkedFolder.eq(
                        copyItemsDTO.newParentFolderId
                    )
                }.map { resultRow ->
                    resultRow[LinksTable.id].value
                }.toList()
            }
            Result.Success(
                response = CopyItemsResponseDTO(
                    folderIds = copyItemsDTO.folderIds, linkIds = copyItemsDTO.linkIds.run {
                        this.toList().mapIndexed { index, pair ->
                            pair.first to linkIds[index]
                        }.toMap()
                    }, correlation = copyItemsDTO.correlation, eventTimestamp = eventTimestamp
                ), webSocketEvent = null
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}