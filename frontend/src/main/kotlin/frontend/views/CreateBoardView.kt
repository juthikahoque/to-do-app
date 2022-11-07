package frontend.views

import frontend.Model
import frontend.interfaces.IView
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import models.Board
import frontend.services.AuthService

class CreateBoardView(private val model: Model): VBox(), IView {

    private val header = Label("Create a Board:")
    private val nameInput = TextField()
    private val nameSection = VBox(nameInput)

    private val createButton = Button("Create").apply{
        background = Background(BackgroundFill(Color.LIGHTGREEN, CornerRadii(2.5), null))
        setOnMouseClicked {
            if(nameInput.text == "") {
                println("No board name entered! This is probably an error!")
                model.addBoard(Board("NO NAME", mutableSetOf(AuthService.user.localId)))
            } else {
                model.addBoard(Board(nameInput.text, mutableSetOf(AuthService.user.localId)))
            }
        }
    }

    private val cancelButton = Button("Cancel").apply{
        background = Background(BackgroundFill(Color.INDIANRED, CornerRadii(2.5), null))
        setOnMouseClicked {
            nameInput.text = ""
            model.setCreateBoardMenu(false)
        }
    }

    override fun updateView() {

    }

    init {
        nameInput.promptText = "Board Name"

        header.font = Font(18.0)
        padding = Insets(20.0)

        spacing = 20.0
        val buttons = HBox(createButton, cancelButton).apply{
            spacing = 10.0
        }
        setMaxSize(300.0, 150.0)
        background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
        children.addAll(header, nameSection, buttons)
    }
}