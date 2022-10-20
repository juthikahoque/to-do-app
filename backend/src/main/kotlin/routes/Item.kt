package routes

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import java.util.UUID

import models.*
import services.*

val itemService = ItemService()

fun Route.itemRouting() {
    route("/items") {

    }

    route("/board/{bid?}/items") {
        get {
            val boardId = UUID.fromString(call.parameters["bid"])
            call.response.status(HttpStatusCode.OK)
            call.respond(itemService.getAllItems(boardId))
        }
        get("{id?}") {
            val id = UUID.fromString(call.parameters["id"])
            call.respond(itemService.getItem(id))
        }
        post {
            val boardId = UUID.fromString(call.parameters["bid"])
            val item = call.receive<Item>()

            if (item.boardId != boardId) {
                call.response.status(HttpStatusCode.NotFound)
                return@post
            }

            itemService.addItem(item)
            call.response.status(HttpStatusCode.Created)
            call.respond(item)
        }
        put {
            val item = call.receive<Item>()
            itemService.updateItem(item)
            call.response.status(HttpStatusCode.OK)
            call.respond(item)
        }
        delete("{id?}") {
            val id = UUID.fromString(call.parameters["id"])
            itemService.deleteItem(id)
            call.response.status(HttpStatusCode.NoContent)
        }
    }
}