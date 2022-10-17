import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.*
import models.Board

/* The view for the sidebar which includes
    - The current user's username
    - A scrollable list of boards available to the user
    - Buttons that allow for new boards to be created and the user to logout
 */
class SidebarView(private val model:Model): BorderPane(), IView{


    private val username = HBox(Label("Current User").apply {
        textFill = Color.WHITE
        font = Font(Font.getDefault().name, 25.0)
    })

    private val group = ToggleGroup().apply {
        selectedToggleProperty().addListener { _, oldValue, newValue ->
            if (newValue == null) oldValue.isSelected = true
        }
    }

    //List of available boards
    private  val boardArea = VBox().apply {
        spacing = 10.0
    }

    private val newBoardButton = Button("Create Board").apply {
        //TODO: Add styles in CSS sheet with additional hover properties
        style = "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54)"
        textFill = Color.LIGHTGREEN
        font = Font(Font.getDefault().name, 15.0)
        this.addEventHandler(MouseEvent.MOUSE_CLICKED){
            model.addBoard(Board("New Board"))
        }
    }

    private val logoutButton = Button("Logout").apply {
        //TODO: Add styles in CSS sheet with additional hover properties
        style = "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54)"
        textFill = Color.INDIANRED
        font = Font(Font.getDefault().name, 15.0)
        this.addEventHandler(MouseEvent.MOUSE_CLICKED){
            model.logout()
        }
    }

    override fun updateView(){
        //reset center
        center = null

        //append new added boards to board area
        val currBoards = model.getBoards()
        for (i in boardArea.children.size until currBoards.size){
            boardArea.children.add(ToggleButton(currBoards[i].name).apply {
                textFill = Color.WHITE
                //TODO: Add styles in CSS sheet with additional hover properties
                style = "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54)"
                toggleGroup = group
                font = Font(Font.getDefault().name, 15.0)
                selectedProperty().addListener { _, _, newValue ->
                    style = if(isSelected){
                        "-fx-background: rgb(169, 169, 169);\n -fx-background-color: rgb(169, 169, 169)"                    //TODO: update views to show items in the selected board
                    } else {
                        "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54)"
                    }
                }

                //first board ("all") is selected by default
                if(i == 0) { isSelected = true }
            })
        }

        //display new board area, which is scrollable
        center = ScrollPane(boardArea).apply {
            style = "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54)"
            isFitToWidth = true
        }
    }

    init {
        padding = Insets(20.0)
        prefWidth = 200.0
        background = Background(BackgroundFill(Color.web("#343436"), null, null))

        top = username
        bottom = VBox(newBoardButton, logoutButton)
        model.addView(this)
    }
}