package frontend

import frontend.controllers.LoginController
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import frontend.views.Presenter

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

        // create an instance of the model
        val model = Model()

        // instantiate the root container for the application
        val presenter = Presenter(model)

        // set the scene
        val scene = Scene(presenter, 800.0, 600.0)

        // set the stage
        stage.title = "To-Do App"
        stage.minWidth = 800.0
        stage.minHeight = 600.0
        stage.maxHeight = Double.MAX_VALUE
        stage.maxWidth = Double.MAX_VALUE
        stage.scene = scene
        stage.show()
    }

    fun changeScene(sceneName: String) {
        val fxmlLoader = FXMLLoader(Main::class.java.getResource(sceneName))
        val scene = Scene(fxmlLoader.load(), 320.0, 240.0)
        stage.scene = scene
    }
}
