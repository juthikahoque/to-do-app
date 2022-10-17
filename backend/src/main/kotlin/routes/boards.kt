package routes

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import java.util.UUID

import models.*
import services.*

val boardService = BoardService()

fun Route.boardRouting() {
    route("/board") {
        get {
            call.response.status(HttpStatusCode.OK)
            call.respond(boardService.getBoards())
        }
        get("{id?}") {
            val id = UUID.fromString(call.parameters["id"])
            call.response.status(HttpStatusCode.OK)
            call.respond(boardService.getBoard(id))
        }
        post {
            val board = call.receive<Board>()
            boardService.addBoard(board)
            call.response.status(HttpStatusCode.Created)
            call.respond(board.id)
        }
        put {
            val board = call.receive<Board>()
            boardService.updateBoard(board)
            call.response.status(HttpStatusCode.OK)
            call.respond(board.id)
        }
        delete("{id?}") {
            val id = UUID.fromString(call.parameters["id"])
            boardService.deleteBoard(id)
            call.response.status(HttpStatusCode.NoContent)
        }
    }
}