package frontend.views

import frontend.Model
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font

class HotkeysModalView(private val model: Model) : VBox() {

    private val hotkeysMap = mapOf<String, String>(
        "Ctrl+N" to "Create new todo item",
        "Ctrl+Shift+N" to "Create new todo board",
        "Ctrl+I" to "Navigate to items area",
        "Ctrl+B" to "Navigate to boards area",
        "Ctrl+C" to "Copy selected item",
        "Ctrl+X" to "Cut selected item",
        "Ctrl+V" to "Paste copied/cut item",
        "Ctrl+F" to "Search",
        "Ctrl+U" to "Add user to the board",
        "Enter" to "Edit selected item or board",
        "Del" to "Delete selected item or board",
        "Ctrl+Z" to "Undo action",
        "Ctrl+Shift+Z" to "Redo action",
        "Ctrl+R" to "Refresh boards and items",
        "Ctrl+Down" to "Move selected item or board down",
        "Ctrl+Up" to "Move selected item or board up",
        "H" to "Toggle this help menu"
    )

    private val heading = Label().apply {
        text = "Hotkeys Help"
        font = Font(18.0)
    }

    private val hotkeysBox = VBox().apply {
        spacing = 10.0
        for (key in hotkeysMap.keys) {
            children.add(TextField(key + " = " + hotkeysMap[key]).apply {
                isEditable = false
                isMouseTransparent = true
            })
        }
    }


    private val closeButton = Button("Close").apply {
        background = Background(BackgroundFill(Color.INDIANRED, CornerRadii(2.5), null))
        setOnAction {
            model.additionalModalView.set("")
        }
        isCancelButton = true
    }

    private val spacer = Pane().apply {
        HBox.setHgrow(this, Priority.ALWAYS)
    }
    private val buttons = HBox(spacer, closeButton).apply {
        spacing = 10.0
    }

    init {
        id = "modal"
        padding = Insets(20.0)
        spacing = 10.0
        setVgrow(this, Priority.ALWAYS)
        children.addAll(heading, hotkeysBox, buttons)
        maxWidth = 300.0
        maxHeight = 350.0
        background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
    }
}
