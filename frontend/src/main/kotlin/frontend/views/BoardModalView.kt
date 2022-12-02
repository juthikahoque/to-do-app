package frontend.views

import frontend.Main
import frontend.Model
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import models.Board
import frontend.services.AuthService
import frontend.services.BoardService
import frontend.services.ItemService
import frontend.utils.Actions
import frontend.utils.UndoRedoManager
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch

class BoardModalView(private val model: Model, private val inputBoard: Board?) : VBox(), CoroutineScope {
    override val coroutineContext = Dispatchers.JavaFx

    private val board = inputBoard ?: Board(
        users = mutableSetOf(AuthService.user)
    )

    private val heading = Label().apply {
        text = if (inputBoard != null) {
            "Edit Board"
        } else {
            "Create Board"
        }
        font = Font(18.0)
    }

    private val deleteButton = Button().apply {
        id = "toggle"
        isVisible = inputBoard != null
        val deleteImage = Image(
            Main::class.java.getResource("/icons/ui_icons/delete.png")!!.toExternalForm(),
            24.0,
            24.0,
            false,
            false
        )
        translateY = -10.0
        graphic = ImageView(deleteImage)
        setOnAction {
            model.boards.remove(board)
            model.additionalModalView.set("")
            launch {
                BoardService.deleteBoard(board.id)
            }
        }
    }

    private val header = HBox().apply {
        val spacer = Pane().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        }
        children.addAll(heading, spacer, deleteButton)
    }

    private val nameInput = TextField().apply {
        promptText = "Board Name"
        text = board.name
    }


    private val createButton = Button("Save").apply {
        id = "save"
        background = Background(BackgroundFill(Color.LIGHTGREEN, CornerRadii(2.5), null))
        isDefaultButton = true

        setOnAction {
            val name = if (nameInput.text == "") {
                println("No board name entered! This is probably an error!")
                "NO NAME"
            } else {
                nameInput.text
            }

            val updatedBoard = board.copy(
                name = name
            )
            if (inputBoard == null) {
                UndoRedoManager.handleAction(Actions.ADD_BOARD, model.items, model.boards, null)
                model.boards.add(updatedBoard)
                launch {
                    BoardService.addBoard(updatedBoard)
                }
            } else {
                UndoRedoManager.handleAction(Actions.UPDATE_BOARD, model.items, model.boards, null)
                val index = model.boards.indexOf(inputBoard)
                model.boards[index] = updatedBoard
                launch {
                    BoardService.updateBoard(updatedBoard)
                }
            }
            model.additionalModalView.set("")
        }
    }

    private val cancelButton = Button("Cancel").apply {
        background = Background(BackgroundFill(Color.INDIANRED, CornerRadii(2.5), null))
        setOnAction {
            nameInput.text = ""
            model.additionalModalView.set("")
        }
        isCancelButton = true
    }

    private val spacer = Pane().apply {
        HBox.setHgrow(this, Priority.ALWAYS)
    }
    private val buttons = HBox(spacer, cancelButton, createButton).apply {
        spacing = 10.0
    }

    init {
        id = "modal"
        padding = Insets(20.0)
        spacing = 10.0
        setVgrow(this, Priority.ALWAYS)
        children.addAll(header, nameInput, buttons)
        maxWidth = 300.0
        maxHeight = 150.0
        background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))

        Platform.runLater { nameInput.requestFocus() }
    }
}