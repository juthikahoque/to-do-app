package frontend.views

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

class CreateBoardView(private val model: Model): VBox() {

    private val header = Label("Create a Board:").apply {
        font = Font(18.0)
    }

    private val nameInput = TextField().apply {
        promptText = "Board Name"
    }

    private val nameSection = VBox(nameInput)


    private val createButton = Button("Create").apply{
        id = "save"
        background = Background(BackgroundFill(Color.LIGHTGREEN, CornerRadii(2.5), null))
        setOnAction {
            if (nameInput.text == "") {
                println("No board name entered! This is probably an error!")
                model.addBoard(Board("NO NAME", mutableSetOf(AuthService.user)))
            } else {
                model.addBoard(Board(nameInput.text, mutableSetOf(AuthService.user)))
            }
            model.additionalModalView.set("")
        }
        isDefaultButton = true
    }

    private val cancelButton = Button("Cancel").apply{
        id = "cancel"
        background = Background(BackgroundFill(Color.INDIANRED, CornerRadii(2.5), null))
        setOnAction {
            nameInput.text = ""
            model.additionalModalView.set("")
        }
        isCancelButton = true
    }

    private val buttons = HBox(createButton, cancelButton).apply{
        spacing = 10.0
    }

    init {
        padding = Insets(20.0)
        spacing = 20.0
        maxWidth = 300.0
        maxHeight = 150.0
        background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
        children.addAll(header, nameSection, buttons)
    }
}