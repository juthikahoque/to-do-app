import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.*
import javafx.stage.Stage
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import services.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

class Main: Application() {
    override fun start(stage: Stage) {
        runBlocking { setupHttpClient() }
        val hbox = HBox()

        val model = Model()

        val sidebar = SidebarView(model)
        val board = BoardView(model)

        val createBoard = CreateBoardView(model)


        hbox.children.addAll(sidebar, VBox(board, createBoard))
        HBox.setHgrow(board, Priority.ALWAYS)

        // set the scene
        val scene = Scene(hbox, 800.0, 600.0)

        // set the stage
        stage.title = "To-Do App"
        stage.minWidth = 800.0
        stage.minHeight = 600.0
        stage.scene = scene
        stage.show()

    }

    suspend fun setupHttpClient() {
        AuthService.init()
        AuthService.googleAuth()

        val client = HttpClient() {
            install(ContentNegotiation) {
                json()
            }
            defaultRequest {
                url("http://127.0.0.1:8080")
                bearerAuth(AuthService.user!!.idToken)
            }
        }

        BoardService.init(client)
        ItemService.init(client)
    }
}