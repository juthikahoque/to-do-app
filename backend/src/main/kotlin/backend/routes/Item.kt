package backend.routes

import backend.services.ItemService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import models.Item
import models.Label
import java.util.*
import java.time.LocalDateTime

fun Route.itemRouting() {
    authenticate {
        route("/board/{bid?}/items") {
            get {
                val boardId = UUID.fromString(call.parameters["bid"])

                // for filtering
                val filterByPriority = call.request.queryParameters.getAll("priority")
                val filterByLabel = call.request.queryParameters.getAll("label")
                val filterByDate = call.request.queryParameters.getAll("date")

                // for sorting
                val sortBy = call.request.queryParameters["sortBy"]
                val orderBy = call.request.queryParameters["orderBy"]

                // for searching
                val search = call.request.queryParameters["search"]

                if (filterByPriority != null) {
                    val priorities = mutableSetOf<Int>()
                    filterByPriority.forEach { priorities.add(it.toInt()) }
                    call.response.status(HttpStatusCode.OK)
                    call.respond(ItemService.filterByPriority(priorities, boardId, sortBy))
                } else if (filterByLabel != null) {
                    val labels = mutableSetOf<Label>()
                    filterByLabel.forEach { labels.add(Label(it)) }
                    call.response.status(HttpStatusCode.OK)
                    call.respond(ItemService.filterByLabel(labels, boardId, sortBy))
                } else if (filterByDate != null) {
                    val startDate = LocalDateTime.parse(filterByDate[0])
                    val endDate = if(filterByDate[1] != "") LocalDateTime.parse(filterByDate[1]) else null
                    call.response.status(HttpStatusCode.OK)
                    call.respond(ItemService.filterByDate(startDate, boardId, endDate, sortBy))
                } else if (sortBy != null) {
                    call.response.status(HttpStatusCode.OK)
                    call.respond(ItemService.sortItems(boardId, sortBy, orderBy))
                } else if (search != null) {
                    call.response.status(HttpStatusCode.OK)
                    call.respond(ItemService.searchByText(boardId, search))
                } else {
                    call.response.status(HttpStatusCode.OK)
                    call.respond(ItemService.getAllItems(boardId))
                }
            }
            get("{id?}") {
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
            delete("{id?}") {
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
        }
    }
}