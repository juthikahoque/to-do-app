import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.*
import javafx.stage.Stage
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import services.*
import kotlinx.coroutines.*

class Main: Application() {
    override fun start(stage: Stage) {
        val pane = BorderPane()

        val model = Model()

        val sidebar = SidebarView(model)

        pane.apply {
            left = sidebar
        }

        // set the scene
        val scene = Scene(pane, 800.0, 600.0)

        // set the stage
        stage.title = "To-Do App"
        stage.minWidth = 800.0
        stage.minHeight = 600.0
        stage.scene = scene
        stage.show()

        runBlocking { setupHttpClient() }
    }

    fun setupHttpClient() {
        val client = HttpClient() {
            install(ContentNegotiation) {
                json()
            }
            defaultRequest {
                url("http://127.0.0.1:8080")

            }
        }
        BoardService.init(client)
        ItemService.init(client)
    }
}