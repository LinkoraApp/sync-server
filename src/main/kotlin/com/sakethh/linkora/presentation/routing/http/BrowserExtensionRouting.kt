package com.sakethh.linkora.presentation.routing.http

import Colors
import com.sakethh.linkora.authenticate
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.Correlation
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.repository.FoldersRepo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import sakethh.kapsule.*
import sakethh.kapsule.utils.*

fun Application.browserExtensionRouting(foldersRepo: FoldersRepo) {
    routing {
        authenticate {
            get("/browser-extension") {
                call.respondText(text = createHTML().html {
                    Surface(
                        onTheHeadElement = {
                            unsafe {
                                +"""
                           <script src="actual.js" type="module"></script>""".trimIndent()
                            }
                        },
                        fonts = listOf(
                            "https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap",
                            "https://fonts.googleapis.com/icon?family=Material+Icons+Outlined"
                        ),
                        modifier = Modifier.width(400.px).height(250.px).backgroundColor(color = Colors.surfaceDark)
                            .boxSizing(BoxSizing.BorderBox).margin(0.px).padding(0.px).custom("overflow: hidden;")
                    ) {
                        runBlocking {
                            BrowserExtensionUI(foldersRepo)
                        }
                    }
                }, contentType = ContentType.Text.Html)
            }
        }
    }
}

private suspend fun BODY.BrowserExtensionUI(foldersRepo: FoldersRepo) {
    val folders = (foldersRepo.getRootFolders() as Result.Success).response
    Column(modifier = Modifier.margin(15.px)) {
        Text(
            text = "Link address",
            fontFamily = "Inter",
            fontWeight = FontWeight.Predefined.Normal,
            fontSize = 10.px,
            color = Colors.onSurfaceDark,
        )
        Text(
            id = "link",
            text = "#LINK_PLACEHOLDER#",
            fontFamily = "Inter",
            fontWeight = FontWeight.Predefined.Medium,
            fontSize = 12.px,
            color = Colors.onSurfaceDark,
            modifier = Modifier.margin(bottom = 10.px)
        )
        Text(
            text = "Title for the link",
            fontFamily = "Inter",
            fontWeight = FontWeight.Predefined.Normal,
            fontSize = 14.px,
            color = Colors.onSurfaceDark,
            modifier = Modifier.margin(bottom = 5.px)
        )
        input(type = InputType.text) {
            id = "link_title"
            style = Modifier.backgroundColor(Colors.codeblockBG).color(Colors.onSurfaceDark).height(25.px)
                .margin(bottom = 10.px).padding(end = 15.px).fontFamily("Inter").toString()
        }
        Text(
            text = "Note for saving the link",
            fontFamily = "Inter",
            fontWeight = FontWeight.Predefined.Normal,
            fontSize = 14.px,
            color = Colors.onSurfaceDark,
            modifier = Modifier.margin(bottom = 5.px)
        )
        input(type = InputType.text) {
            id = "link_note"
            style = Modifier.backgroundColor(Colors.codeblockBG).color(Colors.onSurfaceDark).height(25.px)
                .margin(bottom = 10.px).padding(end = 15.px).fontFamily("Inter").toString()
        }
        Text(
            text = "Total root folders found: ${folders.size}",
            fontFamily = "Inter",
            fontWeight = FontWeight.Predefined.Thin,
            fontSize = 14.px,
            color = Colors.onSurfaceDark,
        )
        Spacer(modifier = Modifier.height(8.5.px))

        Text(
            text = "Save to folder:",
            fontFamily = "Inter",
            fontWeight = FontWeight.Predefined.SemiBold,
            fontSize = 16.px,
            color = Colors.primaryDark,
        )
        Spacer(modifier = Modifier.height(10.px))

        FolderComponent(
            icon = "link", folder = Folder(
                id = -1, name = "Saved Links", note = "", parentFolderId = 0, isArchived = false, eventTimestamp = 0
            )
        )

        FolderComponent(
            icon = "star_outline", folder = Folder(
                id = -2, name = "Important Links", note = "", parentFolderId = 0, isArchived = false, eventTimestamp = 0
            )
        )
        runBlocking {
            folders.forEach { folder ->

                val childFolders = (foldersRepo.getChildFolders(
                    idBasedDTO = IDBasedDTO(
                        id = folder.id,
                        correlation = Correlation(id = "", clientName = ""),
                        eventTimestamp = 1527,
                    )
                ) as Result.Success).response

                FolderComponent(
                    showDivider = childFolders.isEmpty(),
                    folder = folder,
                    bottomSpacing = if (childFolders.isEmpty()) 12 else 8
                )
                if (childFolders.isNotEmpty()) {
                    ChildFoldersComponent(foldersRepo = foldersRepo, folders = childFolders)
                    Spacer(
                        modifier = Modifier.height(8.px)
                    )
                    Spacer(
                        modifier = Modifier.fillMaxWidth(0.95).margin(start = 10.px, end = 15.px, bottom = 12.px)
                            .padding(end = 15.px).height(0.75.px).opacity(0.15).backgroundColor(Colors.outlineDark)
                            .borderRadius(5.px)
                    )
                }
            }
        }
    }
}

private suspend fun DIV.ChildFoldersComponent(
    marginStart: Int = 5, folders: List<Folder>, foldersRepo: FoldersRepo
) {
    folders.forEach {
        Row {
            Spacer(
                modifier = Modifier.width((marginStart - 2).px).backgroundColor(Colors.primaryContainerDark).opacity(
                    0.15
                )
            )
            Spacer(modifier = Modifier.width(5.px))
            FolderComponent(showDivider = false, folder = it, bottomSpacing = 8)
        }
        Spacer(modifier = Modifier.height(5.px))
        val childFolders = (foldersRepo.getChildFolders(
            idBasedDTO = IDBasedDTO(
                id = it.id,
                correlation = Correlation(id = "", clientName = ""),
                eventTimestamp = 1527,
            )
        ) as Result.Success).response

        if (childFolders.isNotEmpty()) {
            Spacer(modifier = Modifier.height(5.px))
            ChildFoldersComponent(
                marginStart = marginStart + 5, foldersRepo = foldersRepo, folders = childFolders
            )
        }
    }
}

private fun DIV.FolderComponent(
    folder: Folder, showDivider: Boolean = true, icon: String = "folder", bottomSpacing: Int = 12
) {
    Row(horizontalAlignment = HorizontalAlignment.Center, modifier = Modifier.cursor(Cursor.Pointer)) {
        span(classes = "material-icons-outlined") {
            style = Modifier.color(
                Colors.primaryDark
            ).blockSelection().size(24.px).toString()
            +icon
        }
        Spacer(modifier = Modifier.width(8.5.px))
        Text(
            text = folder.name,
            fontFamily = "Inter",
            fontWeight = FontWeight.Predefined.Normal,
            fontSize = 16.px,
            color = Colors.primaryDark,
            modifier = Modifier.blockSelection()
        )
    }
    if (showDivider) {
        Spacer(
            modifier = Modifier.height(12.px)
        )

        Spacer(
            modifier = Modifier.fillMaxWidth(0.95).margin(start = 10.px, end = 15.px).padding(end = 15.px)
                .height(0.75.px).opacity(0.15).backgroundColor(Colors.outlineDark).borderRadius(5.px)
        )
    }
    Spacer(
        modifier = Modifier.height(bottomSpacing.px)
    )
}

private fun Modifier.blockSelection() = this.custom("user-select: none;")