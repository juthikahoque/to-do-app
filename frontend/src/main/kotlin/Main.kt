import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.*
import javafx.stage.Stage
import java.awt.Color

class Main: Application() {
    override fun start(stage: Stage) {
        val pane = BorderPane()

        val model = Model()

        val sidebarController = SidebarController(model)
        val sidebar = SidebarView(model, sidebarController)

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
    }
}