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
    authenticate {
        route("/user") {
            get {
                val user = call.principal<User>()!!
                call.response.status(HttpStatusCode.OK)
                call.respond(user)
            }
        }
    }
}