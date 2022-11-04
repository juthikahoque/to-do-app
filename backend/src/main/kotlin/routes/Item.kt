package routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import models.Item
import models.Label
import services.ItemService
import java.util.*
import java.time.LocalDateTime

fun Route.itemRouting() {
    authenticate {
        route("/items") {

        }

        route("/board/{bid?}/item/{filterBy?}") {
            get {
                val boardId = UUID.fromString(call.parameters["bid"])
                val filterBy = call.parameters["filterBy"]
                if(filterBy == "dueDate") {
                    val item = call.receive<MutableSet<String?>>()
                    val endDate = if(item.elementAt(1) != null) LocalDateTime.parse(item.elementAt(1)) else null
                    call.response.status(HttpStatusCode.OK)
                    call.respond(ItemService.filterByDate(LocalDateTime.parse(item.elementAt(0)), boardId, endDate))
                } else if(filterBy == "label") {
                    val labels = call.receive<MutableSet<Label>>()
                    call.respond(ItemService.filterByLabel(labels, boardId))
                } else if(filterBy == "priority") {
                    val priorities = call.receive<MutableSet<Int>>()
                    call.respond(ItemService.filterByPriority(priorities, boardId))
                }
            }
        }

        route("/board/{bid?}/items") {
            get {
                val boardId = UUID.fromString(call.parameters["bid"])
                call.response.status(HttpStatusCode.OK)
                call.respond(ItemService.getAllItems(boardId))
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
        }
    }
}