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

        route("/board/{bid?}/items") {
            get {
                val boardId = UUID.fromString(call.parameters["bid"])
                val filterByPriority = call.request.queryParameters.getAll("priority")
                val filterByLabel = call.request.queryParameters.getAll("label")
                val filterByDate = call.request.queryParameters.getAll("date")


                if (filterByPriority != null) {
                    val priorities = mutableSetOf<Int>()
                    for(s in filterByPriority) {
                        priorities.add(s.toInt())
                        println(s)
                    }
                    call.respond(ItemService.filterByPriority(priorities, boardId))
                } else if (filterByLabel != null) {
                    val labels = mutableSetOf<Label>()
                    for(label in filterByLabel) {
                        labels.add(Label(label))
                    }
                    call.respond(ItemService.filterByLabel(labels, boardId))
                } else if (filterByDate != null) {
                    val startDate = LocalDateTime.parse(filterByDate[0])
                    val endDate = if(filterByDate[1] != "") LocalDateTime.parse(filterByDate[1]) else null
                    call.respond(ItemService.filterByDate(startDate, boardId, endDate))
                } else {
                    println("null req")
                    call.respond(ItemService.getAllItems(boardId))
                }
                call.response.status(HttpStatusCode.OK)
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