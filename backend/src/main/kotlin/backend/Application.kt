package backend

import backend.routes.*
import backend.services.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.AppError

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureServices()
    configureRouting()
    configureAuth()
    configureErrorHandling()
    configureSerialization()
}

fun Application.configureAuth() {
    install(Authentication) {
        val provider = FirebaseAuthenticationProvider(null)
        register(provider)
    }
}

fun Application.configureRouting() {
    routing {
        boardRouting()
        itemRouting()
        userRouting()
        healthRouting()
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<AppError> { call, cause ->
            println(cause.message)
            when (cause.type) {
                AppError.NotFound ->
                    call.respond(HttpStatusCode.NotFound, cause.message ?: AppError.NotFound)
                AppError.Unexpected ->
                    call.respond(HttpStatusCode.InternalServerError, cause.message ?: AppError.Unexpected)
                AppError.BadRequest ->
                    call.respond(HttpStatusCode.BadRequest, cause.message ?: AppError.BadRequest)
                else ->
                    call.respond(HttpStatusCode.InternalServerError, cause.message ?: AppError.Unexpected)
            }
        }
        exception<Throwable> { call, cause ->
            println(cause.message)
            if (cause.message == "not found") {
                call.respond(HttpStatusCode.NotFound, cause.message ?: "unexpected")
            } else {
                call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Unexpected")
            }
        }
    }
}

fun Application.configureServices() {
    val conn = Database().connect("todo")
    BoardService.init(conn)
    ItemService.init(conn)
}
