package frontend

import frontend.services.WindowPreferences
import frontend.views.Presenter
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.input.KeyCombination
import javafx.stage.Stage

lateinit var app: Main

class Main : Application() {

    private lateinit var stage: Stage

    private lateinit var windowPreferences: WindowPreferences

    private val hotkeys = mutableMapOf<KeyCombination, Runnable>()

    override fun start(stage: Stage) {
        app = this
        this.stage = stage

        stage.title = "2+Do"

        windowPreferences = WindowPreferences(stage)

        changeScene("login")
        stage.show()
    }

    fun switchToMain() {
        val sceneName = "main"

        stage.hide()
        // create an instance of the model
        val model = Model()

        // instantiate the root container for the application
        val presenter = Presenter(model)

        // set the scene
        val scene = Scene(presenter)
        stage.scene = scene

        for (hotkey in hotkeys) {
            scene.accelerators[hotkey.key] = hotkey.value
        }
        hotkeys.clear()

        windowPreferences.changeScene(sceneName)
        stage.show()
    }

    fun changeScene(sceneName: String) {
        stage.hide()

        val fxmlLoader = FXMLLoader(Main::class.java.getResource("/views/$sceneName-view.fxml"))
        val scene = Scene(fxmlLoader.load())
        stage.scene = scene

        for (hotkey in hotkeys) {
            scene.accelerators[hotkey.key] = hotkey.value
        }
        hotkeys.clear()

        windowPreferences.changeScene(sceneName)
        stage.show()
    }

    fun addHotkey(key: KeyCombination, func: () -> Unit) {
        hotkeys[key] = Runnable { func() }
    }
}
