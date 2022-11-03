import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.*
import javafx.stage.Stage
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import javafx.fxml.FXMLLoader

import controller.*

lateinit var app: Main

class Main: Application() {

    private lateinit var stage: Stage

    override fun start(stage: Stage) {
        app = this

        val fxmlLoader = FXMLLoader(LoginController::class.java.getResource("/views/login-view.fxml"))
        val scene = Scene(fxmlLoader.load(), 320.0, 240.0)
        stage.title = "To-Do App"
        stage.scene = scene
        stage.show()

        this.stage = stage
    }

    fun switchToMain() {
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

    fun changeScene(sceneName: String) {
        val fxmlLoader = FXMLLoader(Main::class.java.getResource(sceneName))
        val scene = Scene(fxmlLoader.load(), 320.0, 240.0)
        stage.scene = scene
    }
}
