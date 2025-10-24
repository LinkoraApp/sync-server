package com.sakethh.linkora.presentation.routing

import Colors
import com.sakethh.linkora.Constants
import com.sakethh.linkora.ServerConfiguration
import com.sakethh.linkora.authenticate
import com.sakethh.linkora.data.repository.*
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.model.ServerConfig
import com.sakethh.linkora.domain.repository.*
import com.sakethh.linkora.presentation.routing.http.*
import com.sakethh.linkora.utils.SysEnvKey
import com.sakethh.linkora.utils.useSysEnvValues
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.ScriptType
import kotlinx.html.html
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.unsafe
import org.jetbrains.exposed.v1.jdbc.Database
import sakethh.kapsule.*
import sakethh.kapsule.utils.BoxSizing
import sakethh.kapsule.utils.Cursor
import sakethh.kapsule.utils.FontWeight
import sakethh.kapsule.utils.px
import java.io.File
import java.io.FileInputStream
import java.net.InetAddress
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.pathString

fun Application.configureRouting(serverConfig: ServerConfig, markdownManagerRepo: MarkdownManagerRepo) {
    routing {
        authenticate {
            get("/") {
                call.respond(message = HttpStatusCode.OK, status = HttpStatusCode.OK)
            }
            get(Route.Sync.TEST_BEARER.name) {
                call.respond(message = HttpStatusCode.OK, status = HttpStatusCode.OK)
            }
            post(path = "/generate/certs-and-keystore") {
                val zipFile = ServerConfiguration.jarDir.resolve("linkora-certs-and-keystore.zip").run {
                    if (exists()) {
                        this.toFile()
                    } else {
                        createFile().toFile()
                    }
                }

                ZipOutputStream(zipFile.outputStream()).use { zipOutputStream ->
                    val files = listOf<File>(
                        ServerConfiguration.jarDir.resolve("linkoraServerCert.cer").toFile(),
                        ServerConfiguration.jarDir.resolve("linkoraServerCert.pem").toFile(),
                        ServerConfiguration.jarDir.resolve("linkoraServerCert.jks").toFile(),
                    )

                    files.forEach {
                        if (it.exists()) {
                            it.delete()
                        }
                    }

                    ServerConfiguration.exportSignedCertificates(
                        keyStore = ServerConfiguration.createOrLoadServerKeystore(
                            serverConfig = serverConfig
                        )
                    )
                    try {
                        files.forEach { file ->
                            zipOutputStream.putNextEntry(ZipEntry(file.name))
                            FileInputStream(file).use {
                                it.copyTo(zipOutputStream)
                            }
                            zipOutputStream.closeEntry()
                        }
                    } catch (e: Exception) {
                        call.respond(e.stackTraceToString())
                        return@post
                    } catch (e: Error) {
                        call.respond(e.stackTraceToString())
                        return@post
                    }
                }
                if (call.queryParameters["download"] == "true") {
                    call.respondFile(file = zipFile)
                } else {
                    call.respond("Certificates and keystore have been successfully generated at ${ServerConfiguration.jarDir.pathString}. A ZIP file containing all these files is also saved in the same directory.")
                }
            }
        }

        get("/generate/certs-and-keystore") {
            call.respondText(text = createHTML().html {
                Surface(
                    onTheHeadElement = {
                        unsafe {
                            +"""
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                """.trimIndent()
                        }
                    },
                    onTheBodyElement = {
                        script(type = ScriptType.textJavaScript) {
                            unsafe {
                                +"""
                                   document.addEventListener('DOMContentLoaded', function() {
                                       var generateBtn = document.getElementById('generate-btn');
                                       var downloadBtn = document.getElementById('download-btn');
                                       var authTokenInput = document.getElementById('authToken');

                                       if (generateBtn) {
                                           generateBtn.addEventListener('click', function() {
                                               var token = authTokenInput.value.trim();
                                               if (!token) {
                                                   alert('Authorization token required');
                                                   return;
                                               }
                                               
                                               fetch('/generate/certs-and-keystore', {
                                                   method: 'POST',
                                                   headers: { 'Authorization': 'Bearer ' + token }
                                               })
                                               .then(function(response) {
                                                   if (response.ok) {
                                                       return response.text();
                                                   } else {
                                                       return response.text().then(function(text) {
                                                           throw new Error(text);
                                                       });
                                                   }
                                               })
                                               .then(function(text) {
                                                   alert(text);
                                               })
                                               .catch(function(error) {
                                                   alert('Error: ' + error.message);
                                               });
                                           });
                                       }

                                       if (downloadBtn) {
                                           downloadBtn.addEventListener('click', function() {
                                               var token = authTokenInput.value.trim();
                                               if (!token) {
                                                   alert('Authorization token required');
                                                   return;
                                               }
                                               
                                               fetch('/generate/certs-and-keystore?download=true', {
                                                   method: 'POST',
                                                   headers: { 'Authorization': 'Bearer ' + token }
                                               })
                                               .then(function(response) {
                                                   if (response.ok) {
                                                       return response.blob();
                                                   } else {
                                                       return response.text().then(function(text) {
                                                           throw new Error(text);
                                                       });
                                                   }
                                               })
                                               .then(function(blob) {
                                                   var url = window.URL.createObjectURL(blob);
                                                   var a = document.createElement('a');
                                                   a.href = url;
                                                   a.download = 'linkora-certs-and-keystore.zip';
                                                   document.body.appendChild(a);
                                                   a.click();
                                                   setTimeout(function() {
                                                       document.body.removeChild(a);
                                                       window.URL.revokeObjectURL(url);
                                                   }, 100);
                                               })
                                               .catch(function(error) {
                                                   alert('Error: ' + error.message);
                                               });
                                           });
                                       }
                                   });
                                   """.trimIndent()
                            }
                        }
                    },
                    fonts = listOf(
                        "https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap",
                    ),
                    modifier = Modifier.backgroundColor(color = Colors.surfaceDark).boxSizing(BoxSizing.BorderBox)
                        .margin(0.px).padding(0.px).custom("overflow: hidden;")
                ) {
                    Column(modifier = Modifier.margin(value = 50.px)) {
                        Text(
                            text = "Enter the auth token", fontFamily = "Inter",
                            color = Colors.onSurfaceDark,
                        )
                        Spacer(modifier = Modifier.height(5.px))
                        TextInputField(
                            id = "authToken",
                            value = "",
                            fontWeight = FontWeight.Predefined.Normal,
                            fontSize = 16.px,
                            fontFamily = "Inter",
                            modifier = Modifier.height(25.px).color(Colors.onSurfaceDark)
                                .backgroundColor(Colors.ButtonContentColor)
                        )
                        Spacer(modifier = Modifier.height(10.px))
                        Button(
                            id = "generate-btn",
                            onClick = { "" },
                            modifier = Modifier.height(25.px).backgroundColor(Colors.ButtonContainerColor)
                                .cursor(Cursor.Pointer)
                        ) {
                            Text(
                                fontWeight = FontWeight.Predefined.Medium,
                                text = "Generate",
                                fontFamily = "Inter",
                                color = Colors.ButtonContentColor,
                                fontSize = 16.px
                            )
                        }
                        Spacer(modifier = Modifier.height(10.px))
                        Button(
                            id = "download-btn",
                            onClick = {
                                ""
                            },
                            modifier = Modifier.height(25.px).backgroundColor(Colors.ButtonContainerColor)
                                .cursor(Cursor.Pointer)
                        ) {
                            Text(
                                fontWeight = FontWeight.Predefined.Medium,
                                fontSize = 16.px,
                                text = "Download",
                                fontFamily = "Inter",
                                color = Colors.ButtonContentColor
                            )
                        }
                    }
                }
            }, contentType = ContentType.Text.Html)
        }

        get(Route.Sync.SERVER_IS_CONFIGURED.name) {
            val placeHolderValue =
                if ((useSysEnvValues().not() && serverConfig.hostAddress != InetAddress.getLocalHost().hostAddress) || (useSysEnvValues() && System.getenv(
                        SysEnvKey.LINKORA_HOST_ADDRESS.name
                    ) != InetAddress.getLocalHost().hostAddress)
                ) {
                    """### **Local Hosting & IPv4 Address**
- If you're **hosting locally**, ensure you're using an **IPv4 address** (${InetAddress.getLocalHost().hostAddress}) as `serverHost`.
- If using environment variables, set `${SysEnvKey.LINKORA_HOST_ADDRESS.name}` to `${InetAddress.getLocalHost().hostAddress}`.
- Otherwise, update `serverHost` in `linkoraConfig.json` so the Android app can connect.
- If you're **only using Linkora on Desktop**, no changes are needed."""
                } else {
                    ""
                }
            val requiredHtml = markdownManagerRepo.getRawHtmlBasedOnRawMD(
                "The sync-server version is **${Constants.SERVER_VERSION}**.\n\nYou are currently connected to the **${
                    Database.getDialectName(serverConfig.databaseUrl)
                }** database, which will be **used by the server to store data**."
            ) + markdownManagerRepo.getRawHtmlBasedOnMDFile(
                fileLocation = "/raw/SERVER_IS_CONFIGURED.md", placeHolder = "#{PLACEHOLDER_1}" to placeHolderValue
            )
            call.respondText(contentType = ContentType.Text.Html, text = requiredHtml)
        }

        val linksRepo: LinksRepo = LinksRepoImpl()
        val panelsRepo: PanelsRepo = PanelsRepoImpl()
        val foldersRepo: FoldersRepo = FoldersRepoImpl(panelsRepo)
        val syncRepo: SyncRepo = SyncRepoImpl()
        val multiActionRepo: MultiActionRepo = MultiActionRepoImpl(foldersRepo)
        val tagsRepo: TagsRepo = TagsRepoImpl()
        foldersRouting(foldersRepo)
        linksRouting(linksRepo)
        panelsRouting(panelsRepo)
        syncRouting(syncRepo)
        multiActionRouting(multiActionRepo)
        tagsRouting(tagsRepo)
    }
}
