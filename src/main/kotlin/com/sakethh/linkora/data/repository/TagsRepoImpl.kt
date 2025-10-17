package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.tag.CreateTagDTO
import com.sakethh.linkora.domain.dto.tag.RenameTagDTO
import com.sakethh.linkora.domain.dto.tag.TagDTO
import com.sakethh.linkora.domain.model.WebSocketEvent
import com.sakethh.linkora.domain.repository.TagsRepo
import com.sakethh.linkora.domain.tables.TagsTable
import com.sakethh.linkora.domain.tables.helper.TombStoneHelper
import com.sakethh.linkora.utils.checkForLWWConflictAndThrow
import com.sakethh.linkora.utils.tryAndCatchResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant

class TagsRepoImpl : TagsRepo {
    override suspend fun createATag(createTagDTO: CreateTagDTO): Result<NewItemResponseDTO> {
        return tryAndCatchResult {
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                TagsTable.insertAndGetId {
                    it[TagsTable.name] = createTagDTO.name
                    it[TagsTable.lastModified] = eventTimestamp
                }
            }.value.let { newTagId ->
                Result.Success(
                    response = NewItemResponseDTO(
                        id = newTagId,
                        timeStampBasedResponse = TimeStampBasedResponse(
                            eventTimestamp = eventTimestamp,
                            message = "Tag created successfully with the id = $newTagId",
                        ),
                        correlation = createTagDTO.correlation
                    ),
                    webSocketEvent = WebSocketEvent(
                        operation = Route.Tag.CREATE_TAG.name, payload = Json.encodeToJsonElement(
                            TagDTO(
                                id = newTagId,
                                name = createTagDTO.name,
                                eventTimestamp = eventTimestamp,
                                correlation = createTagDTO.correlation
                            )
                        )
                    )
                )
            }
        }
    }

    override suspend fun renameATag(renameTagDTO: RenameTagDTO): Result<TimeStampBasedResponse> {
        return tryAndCatchResult {
            TagsTable.checkForLWWConflictAndThrow(
                id = renameTagDTO.id,
                timeStamp = renameTagDTO.eventTimestamp,
                lastModifiedColumn = TagsTable.lastModified
            )
            val evenTimeStamp = Instant.now().epochSecond
            transaction {
                TagsTable.update(where = { TagsTable.id eq renameTagDTO.id }) {
                    it[TagsTable.name] = renameTagDTO.newName
                    it[TagsTable.lastModified] = evenTimeStamp
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    eventTimestamp = evenTimeStamp,
                    message = "Tag renamed to ${renameTagDTO.newName}"
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.Tag.RENAME_TAG.name,
                    payload = Json.encodeToJsonElement(renameTagDTO.copy(eventTimestamp = evenTimeStamp))
                )
            )
        }
    }

    override suspend fun deleteATag(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse> {
        return tryAndCatchResult {
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                TombStoneHelper.insert(
                    payload = Json.encodeToString(idBasedDTO.copy(eventTimestamp = eventTimestamp)),
                    operation = Route.Tag.DELETE_TAG.name,
                    deletedAt = eventTimestamp
                )
                TagsTable.deleteWhere {
                    id eq idBasedDTO.id
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    eventTimestamp = eventTimestamp,
                    message = "Tag has been deleted."
                ),
                webSocketEvent = WebSocketEvent(
                    operation = Route.Tag.DELETE_TAG.name,
                    payload = Json.encodeToJsonElement(idBasedDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        }
    }
}