package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.ArchiveMultipleItemsDTO
import com.sakethh.linkora.domain.dto.TimeStampBasedResponse
import com.sakethh.linkora.domain.model.WebSocketEvent
import com.sakethh.linkora.domain.repository.MultiActionRepo
import com.sakethh.linkora.domain.tables.FoldersTable
import com.sakethh.linkora.domain.tables.LinksTable
import com.sakethh.linkora.utils.checkForLWWConflictAndThrow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

class MultiActionRepoImpl : MultiActionRepo {
    override suspend fun archiveMultipleItems(archiveMultipleItemsDTO: ArchiveMultipleItemsDTO): Result<TimeStampBasedResponse> {
        return try {
            LinksTable.checkForLWWConflictAndThrow(
                id = archiveMultipleItemsDTO.linkIds.last(),
                timeStamp = archiveMultipleItemsDTO.eventTimestamp,
                lastModifiedColumn = LinksTable.lastModified
            )
            FoldersTable.checkForLWWConflictAndThrow(
                id = archiveMultipleItemsDTO.folderIds.last(),
                timeStamp = archiveMultipleItemsDTO.eventTimestamp,
                lastModifiedColumn = FoldersTable.lastModified
            )
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
}