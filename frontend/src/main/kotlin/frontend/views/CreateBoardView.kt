package frontend.views

import frontend.interfaces.IView
import frontend.Model
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.*
import javafx.scene.paint.Color
import models.Board
import java.util.*

class CreateBoardView(private val model: Model): VBox(), IView {

    private val nameLabel = Label("Board Name:")
    private val nameInput = TextField()
    private val nameSection = VBox(nameLabel, nameInput)

    private val usersLabel = Label("Users:")
    //TODO: Change to a container that saves previous values
    private val usersInput = TextField()
    private val usersSection = VBox(usersLabel, usersInput)

    private val createButton = Button("Create!").apply{
        background = Background(BackgroundFill(Color.LIGHTGREEN, null, null))
        setOnMouseClicked {
            if(nameInput.text == ""){
                println("No board name entered! This is probably an error!")
                model.addBoard(Board("NO NAME", mutableSetOf(UUID.randomUUID())))
            }
            else{
                model.addBoard(Board(nameInput.text, mutableSetOf(UUID.randomUUID())))
            }
            nameInput.text = ""
            usersInput.text = ""
            model.setCreateBoardMenu(false)
        }
    }

    private val cancelButton = Button("Cancel").apply{
        background = Background(BackgroundFill(Color.INDIANRED, null, null))
        setOnMouseClicked {
            nameInput.text = ""
            usersInput.text = ""
            model.setCreateBoardMenu(false)
        }


    }


    override fun updateView() {
        isVisible = model.showCreateBoard
    }

    init {
        padding = Insets(20.0)
        spacing = 20.0
        val buttons = HBox(createButton, cancelButton).apply{
            spacing = 10.0
        }
        children.addAll(nameSection, usersSection, buttons)
        model.addView(this)

    }







}