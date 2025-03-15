package com.sakethh.linkora.utils

import com.sakethh.linkora.domain.LWWConflictException
import com.sakethh.linkora.domain.tables.FoldersTable
import com.sakethh.linkora.domain.tables.LinksTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction

fun useSysEnvValues(): Boolean {
    return try {
        System.getenv(SysEnvKey.LINKORA_SERVER_USE_ENV_VAL.name).toBooleanStrict()
    } catch (e: Exception) {
        false
    }
}

fun LongIdTable.checkForLWWConflictAndThrow(id: Long, timeStamp: Long, lastModifiedColumn: Column<Long>) {
    transaction {
        this@checkForLWWConflictAndThrow.select(lastModifiedColumn).where {
            this@checkForLWWConflictAndThrow.id.eq(id)
        }.let {
            try {
                val resultRow = it.single()
                if (resultRow[lastModifiedColumn] > timeStamp) {
                    throw LWWConflictException()
                }
            } catch (e: Exception) {
                if (e is LWWConflictException) {
                    throw e
                }
                e.printStackTrace()
            }
        }
    }
}

fun FoldersTable.copy(source: List<ResultRow>, eventTimestamp: Long, parentFolderId: Long?): List<ResultRow> {
    return batchInsert(source) {
        this[folderName] = it[folderName]
        this[lastModified] = eventTimestamp
        this[note] = it[note]
        this[parentFolderID] = parentFolderId
        this[isFolderArchived] = it[isFolderArchived]
    }.toList()
}

fun LinksTable.copy(source: List<ResultRow>, eventTimestamp: Long, parentFolderId: Long?): List<ResultRow> {
    return batchInsert(source) {
        set(lastModified, eventTimestamp)
        set(linkType, it[linkType])
        set(linkTitle, it[linkTitle])
        set(url, it[url])
        set(baseURL, it[baseURL])
        set(imgURL, it[imgURL])
        set(note, it[note])
        set(idOfLinkedFolder, parentFolderId)
        set(userAgent, it[userAgent])
        set(mediaType, it[mediaType])
        set(markedAsImportant, it[markedAsImportant])
    }
}