package frontend

import frontend.services.Settings
import frontend.services.WindowPreferences
import frontend.views.Presenter
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.input.KeyCombination
import javafx.stage.Stage
import jfxtras.styles.jmetro.*

lateinit var app: Main

class Main : Application() {

    lateinit var stage: Stage

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

    fun getTheme(): String {
        val themeStr = Settings.get("themeMode", "")
        return if (themeStr != "") themeStr else "light"
    }

    private fun setTheme(mode: String) {
        println(mode)
        Settings.put("themeMode", mode)
    }

    fun switchToMain() {
        val sceneName = "main"

        stage.hide()
        // create an instance of the model
        val model = Model()

        // instantiate the root container for the application
        val presenter = Presenter(model)

        // set the scene
        val scene = Scene(presenter, 800.0, 600.0)
        stage.scene = scene

        for (hotkey in hotkeys) {
            scene.accelerators[hotkey.key] = hotkey.value
        }
        hotkeys.clear()

        val mode = getTheme()
        changeThemeMode(mode)

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

    fun changeThemeMode(mode: String) {
        setTheme(mode)
        val scene = stage.scene
        scene.stylesheets.clear()
        val jMetro = JMetro(if (mode == "light") Style.LIGHT else Style.DARK)
        jMetro.scene = scene
        scene.stylesheets.add(Main::class.java.getResource("/css/${mode}.css")!!.toExternalForm())
        stage.scene = scene

        stage.show()
    }


    fun addHotkey(key: KeyCombination, func: () -> Unit) {
        hotkeys[key] = Runnable { func() }
    }
}
