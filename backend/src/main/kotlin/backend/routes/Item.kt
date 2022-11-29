package backend.routes

import backend.models.AuthUser
import backend.services.ItemService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.Item
import models.Label
import java.io.File
import java.time.LocalDateTime
import java.util.*

fun Route.itemRouting() {
    authenticate {
        route("/board/{bid?}/items") {
            get {
                val user = call.principal<AuthUser>()!!
                val boardId = call.parameters["bid"]!!

                // for filtering
                val filterByPriority = call.request.queryParameters.getAll("priority")
                val priorities = mutableSetOf<Int>()
                filterByPriority?.forEach { priorities.add(it.toInt()) }

                val filterByLabel = call.request.queryParameters.getAll("label")
                val labels = mutableSetOf<Label>()
                filterByLabel?.forEach { labels.add(Label(it)) }

                val sDateStr = call.request.queryParameters["sDate"]
                val startDate = if (sDateStr == null) null else LocalDateTime.parse(sDateStr)
                val eDateStr = call.request.queryParameters["eDate"]
                val endDate = if (eDateStr == null) null else LocalDateTime.parse(eDateStr)

                // for searching
                val search = call.request.queryParameters["search"]

                // for sorting
                val sortBy = call.request.queryParameters["sortBy"]
                val orderBy = call.request.queryParameters["orderBy"]

                call.response.status(HttpStatusCode.OK)
                call.respond(
                    ItemService.getItems(
                        if (boardId == "all") "" else boardId,
                        user.id,
                        startDate,
                        endDate,
                        labels,
                        priorities,
                        search,
                        sortBy,
                        orderBy
                    )
                )
            }
            get("{id}") {
                val id = UUID.fromString(call.parameters["id"])
                call.respond(ItemService.getItem(id))
            }
            post {
                val boardId = UUID.fromString(call.parameters["bid"])
                val item = call.receive<Item>()

                if (item.boardId != boardId) {
                    call.response.status(HttpStatusCode.NotFound)
                    return@post
                }

                ItemService.addItem(item)
                call.response.status(HttpStatusCode.Created)
                call.respond(item)
            }
            put {
                val item = call.receive<Item>()
                ItemService.updateItem(item)
                call.response.status(HttpStatusCode.OK)
                call.respond(item)
            }
            delete("{id}") {
                val id = UUID.fromString(call.parameters["id"])
                ItemService.deleteItem(id)
                call.response.status(HttpStatusCode.NoContent)
            }

            put("order") {
                val boardId = UUID.fromString(call.parameters["bid"])
                val from = call.request.queryParameters["from"]?.toInt()
                val to = call.request.queryParameters["to"]?.toInt()

                if (from != null && to != null) {
                    ItemService.changeOrder(boardId, from, to)
                    call.response.status(HttpStatusCode.OK)
                    call.respond(ItemService.getAllItems(boardId))
                } else {
                    call.respond(HttpStatusCode.BadRequest, "requires [from] and [to] query parameters")
                }
            }

            // upload file
            post("{id}/file") {
                val id = UUID.fromString(call.parameters["id"])
                val multipart = call.receiveMultipart()

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        // retrieve file name of upload
                        val name = part.originalFileName!!
                        ItemService.addAttachment(id, name, part.streamProvider())
                    }
                    // make sure to dispose of the part after use to prevent leaks
                    part.dispose()
                }

                call.respond(HttpStatusCode.OK)
            }

            // download file
            get("{id}/file/{name}") {
                val id = call.parameters["id"]
                // get filename from request url
                val filename = call.parameters["name"]
                // construct reference to file
                // ideally this would use a different filename
                val file = File("data/$id/$filename")
                if (file.exists()) {
                    call.respondFile(file)
                } else call.respond(HttpStatusCode.NotFound)
            }

            delete("{id}/file/{name}") {
                val id = UUID.fromString(call.parameters["id"])
                // get filename from request url
                val filename = call.parameters["name"]!!
                ItemService.deleteAttachment(id, filename)

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}