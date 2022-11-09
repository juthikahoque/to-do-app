package frontend.views

import frontend.Model
import frontend.interfaces.IView
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font

/* The view for the sidebar which includes
    - The current user's username
    - A scrollable list of boards available to the user
    - Buttons that allow for new boards to be created and the user to logout
 */
class SidebarView(private val model: Model): BorderPane(), IView {


    private val username = HBox(Label("Current User").apply {
        textFill = Color.WHITE
        font = Font( 25.0)
    })

    private val group = ToggleGroup().apply {
        selectedToggleProperty().addListener { _, oldValue, newValue ->
            if (newValue == null) oldValue.isSelected = true
        }
    }

    //List of available boards
    private val boardArea = VBox().apply {
        spacing = 10.0
    }

    private val newBoardButton = Button("Create Board").apply {
        //TODO: Add styles in CSS sheet with additional hover properties
        style = "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54)"
        textFill = Color.LIGHTGREEN
        font = Font( 15.0)

        setOnMouseClicked {
            model.setCreateBoardMenu(true)
        }
    }

    private val logoutButton = Button("Logout").apply {
        //TODO: Add styles in CSS sheet with additional hover properties
        style = "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54)"
        textFill = Color.INDIANRED
        font = Font(15.0)

        setOnMouseClicked {
            model.logout()
        }
    }

    override fun updateView() {
        if (!model.showCreateBoard) {
            boardArea.children.clear()

            model.getBoards().forEachIndexed { index, board ->
                boardArea.children.add(ToggleButton(board.name).apply {
                    textFill = Color.WHITE
                    //TODO: Add styles in CSS sheet with additional hover properties
                    style = "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54);\n -fx-pref-width: 150.0;\n" +
                            " -fx-alignment: CENTER_LEFT"
                    toggleGroup = group
                    font = Font(Font.getDefault().name, 15.0)

                    selectedProperty().addListener { _, _, _ ->
                        style = if (isSelected) {
                            "-fx-background: rgb(169, 169, 169);\n -fx-background-color: rgb(104, 104, 104);\n" +
                                    " -fx-pref-width: 150.0;\n -fx-alignment: CENTER_LEFT"
                        } else {
                            "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54);\n" +
                                    " -fx-pref-width: 150.0;\n -fx-alignment: CENTER_LEFT"
                        }
                    }

                    setOnMouseClicked {
                        model.updateCurrentBoard(index)
                    }

                    //first board ("all") is selected by default
                    if (index == 0) { isSelected = true }
                })
            }


            // display new board area, which is scrollable
            center = ScrollPane(boardArea).apply {
                style = "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54)"
                isFitToWidth = true
            }
        }
    }

    init {
        username.padding = Insets(0.0, 0.0, 20.0, 0.0)

        padding = Insets(10.0)
        minWidth = 175.0
        background = Background(BackgroundFill(Color.web("#343436"), null, null))

        top = username
        bottom = VBox(newBoardButton, logoutButton)
        model.addView(this)
    }
}