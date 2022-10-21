import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import routes.boardRouting
import routes.itemRouting
import services.BoardService
import services.Database
import services.ItemService

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
}

fun Application.configureServices() {
    val conn = Database().connect("todo")
    BoardService.init(conn)
    ItemService.init(conn)
}
