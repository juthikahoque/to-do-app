import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import routes.*
import services.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureServices()
    configureRouting()
    configureSerialization()
}

fun Application.configureRouting() {
    routing {
        boardRouting()
        itemRouting()
    }
}



fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Unexpected")
        }
    }

    install(Authentication) {
        google()
    }
}

fun Application.configureServices() {
    val conn = Database().connect("todo")
    BoardService.init(conn)
    ItemService.init(conn)
    FirebaseAuthService.init()
}
