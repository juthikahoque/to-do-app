package backend.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.AuthUser
import services.UserService

fun Route.userRouting() {
    authenticate {
        route("/user") {
            get {
                val emails = call.request.queryParameters.getAll("emails")
                if (emails != null) {
                    call.response.status(HttpStatusCode.OK)
                    call.respond(UserService.getAllUsersByEmails(emails))
                } else {
                    val user = call.principal<AuthUser>()!!
                    call.response.status(HttpStatusCode.OK)
                    call.respond(UserService.getUserById(user.id))
                }
            }
        }
    }
}