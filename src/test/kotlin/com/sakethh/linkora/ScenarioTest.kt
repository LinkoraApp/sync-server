package com.sakethh.linkora

import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.Correlation
import com.sakethh.linkora.domain.dto.DeleteEverythingDTO
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.folder.AddFolderDTO
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.utils.httpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import java.time.Instant
import kotlin.test.Test

class ScenarioTest {
    private val serverConfig = ServerConfiguration.readConfig()

    private var folderInsertionCount = 0

    private fun newFolder(parentFolderID: Long?): AddFolderDTO {
        return AddFolderDTO(
            note = "", parentFolderId = parentFolderID, isArchived = false, correlation = Correlation(
                id = "", clientName = ""
            ), eventTimestamp = Instant.now().epochSecond, name = "Folder ${++folderInsertionCount}"
        )
    }

    @Test
    fun `insert folders in a hierarchy and delete the root folder`() = testApplication {

        // when I add more tests in the future, this won't be here, or rather, I should say, it shouldn't be here
        application {
            module()
        }

        // my config (linkoraConfig.json) when running tests (or while in development) refers to a database that is not my primary DB, which I use for my Linkora app; it is a dummy DB that I test during development, and now the same is used here, so make sure the config you're referring to actually is a dummy DB and not the production (or the one you actually use for personal use)
        httpClient().post(Route.Sync.DELETE_EVERYTHING.name) {
            contentType(ContentType.Application.Json)
            setBody(DeleteEverythingDTO(0, correlation = Correlation("", "")))
            bearerAuth(serverConfig.serverAuthToken)
        }


        suspend fun createANewFolder(parentFolderID: Long?): Long {
            return httpClient().post(Route.Folder.CREATE_FOLDER.name) {
                contentType(ContentType.Application.Json)
                bearerAuth(serverConfig.serverAuthToken)
                setBody(newFolder(parentFolderID))
            }.body<NewItemResponseDTO>().id
        }

        var levelCount = 0
        suspend fun createFolders(parentFolderID: Long?) {
            ++levelCount
            var newLastFolderId: Long? = null
            repeat(4) { repeatCount ->
                createANewFolder(parentFolderID).also {
                    if (repeatCount == 3) {
                        newLastFolderId = it
                    }
                }
            }
            if (levelCount < 4) {
                createFolders(newLastFolderId)
            }
        }

        val rootFolderId = createANewFolder(null)
        createFolders(parentFolderID = rootFolderId)

        httpClient().get(urlString = Route.Folder.GET_ALL_FOLDERS.name) {
            bearerAuth(serverConfig.serverAuthToken)
        }.body<List<Folder>>().let { list ->

            assert(list.count {
                it.parentFolderId == null
            } == 1)

            val randomParentId = list.filter { it.parentFolderId != null }.random().parentFolderId
            assert(list.filter {
                it.parentFolderId == randomParentId
            }.size == 4)

            println("Before deleting: ${list.size}")

            assert(list.isNotEmpty())
        }

        httpClient().post(urlString = Route.Folder.DELETE_FOLDER.name) {
            bearerAuth(serverConfig.serverAuthToken)
            contentType(ContentType.Application.Json)
            setBody(IDBasedDTO(id = rootFolderId, correlation = Correlation("", ""), 0))
        }

        httpClient().get(urlString = Route.Folder.GET_ALL_FOLDERS.name) {
            bearerAuth(serverConfig.serverAuthToken)
        }.body<List<Folder>>().let {
            println("After deleting: ${it.size}")
            assert(it.isEmpty())
        }
    }

}
