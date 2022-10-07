import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.*
import javafx.stage.Stage

class Main: Application() {
    override fun start(stage: Stage) {
        val pane = Pane()

        // set the scene
        val scene = Scene(pane, 800.0, 600.0)

        // set the stage
        stage.title = "To-Do App"
        stage.minWidth = 800.0
        stage.minHeight = 600.0
        stage.scene = scene
        stage.show()
    }
}