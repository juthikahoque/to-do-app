package backend.routes

import backend.services.BoardService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import backend.models.AuthUser
import models.Board
import java.util.*

fun Route.boardRouting() {
    authenticate {
        route("/board") {
            get {
                val user = call.principal<AuthUser>()!!
                call.response.status(HttpStatusCode.OK)
                call.respond(BoardService.getBoards(user.id))
            }
            get("{id}") {
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
                print("SERVER GOT A BOARD TO UPDATE")
                print(board)
                val updated = BoardService.updateBoard(board)
                if (updated != null) {
                    call.response.status(HttpStatusCode.OK)
                    call.respond(updated)
                } else {
                    call.response.status(HttpStatusCode.NotFound)
                }

            }
            delete("{id}") {
                val id = UUID.fromString(call.parameters["id"])
                BoardService.deleteBoard(id)
                call.response.status(HttpStatusCode.NoContent)
            }
            put("order") {
                val user = call.principal<AuthUser>()!!
                val from = call.request.queryParameters["from"]?.toInt()
                val to = call.request.queryParameters["to"]?.toInt()

                if (from != null && to != null) {
                    BoardService.changeOrder(user.id, from, to)
                    call.response.status(HttpStatusCode.OK)
                    call.respond(BoardService.getBoards(user.id))
                } else {
                    call.respond(HttpStatusCode.BadRequest, "requires [from] and [to] query parameters")
                }
            }
        }
    }
}
