package backend.routes

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.boardRouting() {
    route("/board") {
        get {
            call.respondText("Hello, world!")
        }
        get("{id?}") {

        }
        post {

        }
        put {

        }
        delete("{id?}") {

        }
    }
}