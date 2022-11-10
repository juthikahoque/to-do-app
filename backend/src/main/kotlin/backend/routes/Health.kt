package backend.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import backend.models.AuthUser

fun Route.healthRouting() {
    get("/health") {
        call.respond(HttpStatusCode.OK)
    }

    authenticate {
        get("/auth") {
            val user = call.principal<AuthUser>()!!
            call.respond(HttpStatusCode.OK, user.id)
        }
    }
}
