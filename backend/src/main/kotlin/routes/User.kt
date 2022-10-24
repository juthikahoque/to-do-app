package routes

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import java.util.UUID

import models.*
import services.*

fun Route.userRouting() {
    route("/user") {
        get {
            val user = call.principal<User>()!!
            call.response.status(HttpStatusCode.OK)
            call.respond(BoardService.getBoards(user))
        }
        get("{id?}") {
            val id = UUID.fromString(call.parameters["id"])
            call.response.status(HttpStatusCode.OK)
            call.respond(BoardService.getBoard(id))
        }
        post {
            val board = call.receive<Board>()
            BoardService.addBoard(board)
            call.response.status(HttpStatusCode.Created)
            call.respond(board)
        }
        put {
            val board = call.receive<Board>()
            val updated = BoardService.updateBoard(board)
            if (updated != null) {
                call.response.status(HttpStatusCode.OK)
                call.respond(updated)
            } else {
                call.response.status(HttpStatusCode.NotFound)
            }

        }
        delete("{id?}") {
            val id = UUID.fromString(call.parameters["id"])
            BoardService.deleteBoard(id)
            call.response.status(HttpStatusCode.NoContent)
        }
    }
}