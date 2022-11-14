package frontend.services

import javafx.animation.PauseTransition
import javafx.event.EventHandler
import javafx.stage.Stage
import javafx.util.Duration
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

class WindowPreferences(private val stage: Stage) {
    private val windowSettings: Map<String, WindowSettings> = Json.decodeFromStream(
        this::class.java.classLoader.getResourceAsStream("window-settings.json")!!
    )
    var scene: String? = null
    lateinit var curPref: WindowPreference

    private val pauseTransition = PauseTransition(Duration(0.1)).apply {
        onFinished = EventHandler {
            save(scene)
        }
    }

    init {
        stage.xProperty().addListener { _, _, _ -> pauseTransition.playFromStart() }
        stage.yProperty().addListener { _, _, _ -> pauseTransition.playFromStart() }
        stage.widthProperty().addListener { _, _, _ -> pauseTransition.playFromStart() }
        stage.heightProperty().addListener { _, _, _ -> pauseTransition.playFromStart() }

        stage.onCloseRequest = EventHandler { save(scene) }
    }

    fun changeScene(newScene: String?) {
        save(scene)
        scene = newScene
        set(scene)
    }

    private fun save(scene: String?) {
        if (scene == null) return
        curPref = if (stage.isMaximized) {
            curPref.copy(isMaximized = true)
        } else {
            WindowPreference(
                width = stage.width,
                height = stage.height,
                x = stage.x,
                y = stage.y,
            )
        }
        Settings.put("win.${scene}", Json.encodeToString(curPref))
    }

    private fun set(scene: String?) {
        if (scene == null) return
        val winStr = Settings.get("win.${scene}", "")
        println(winStr)
        curPref = if (winStr != "") Json.decodeFromString(winStr) else WindowPreference()

        stage.width = curPref.width
        stage.height = curPref.height
        stage.x = curPref.x
        stage.y = curPref.y
        stage.isMaximized = curPref.isMaximized

        val winSettings = windowSettings[scene]!!
        stage.minHeight = winSettings.minHeight
        stage.minWidth = winSettings.minWidth
        stage.maxHeight = winSettings.maxHeight
        stage.maxWidth = winSettings.maxWidth
    }

    @Serializable
    data class WindowPreference(
        val width: Double = 600.0,
        val height: Double = 800.0,
        val x: Double = 0.0,
        val y: Double = 0.0,
        val isMaximized: Boolean = false,
    )

    @Serializable
    data class WindowSettings(
        val minHeight: Double = 0.0,
        val minWidth: Double = 0.0,
        val maxHeight: Double = 0.0,
        val maxWidth: Double = 0.0,
    )
}