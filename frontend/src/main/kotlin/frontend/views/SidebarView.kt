package frontend.views

import frontend.Model
import frontend.app
import frontend.interfaces.IView
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font

/* The view for the sidebar which includes
    - The current user's username
    - A scrollable list of boards available to the user
    - Buttons that allow for new boards to be created and the user to logout
 */
class SidebarView(private val model: Model): BorderPane(), IView {

    //List of available boards
    private val boardArea = VBox().apply {
        spacing = 10.0
    }

    private val username = HBox(Label().apply {
        text = model.getUsername()
        textFill = Color.LIGHTBLUE
        font = Font( 20.0)
    })

    private val newBoardButton = Button("Create Board").apply {
        //TODO: Add styles in CSS sheet with additional hover properties
        style = "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54)"
        textFill = Color.LIGHTGREEN
        font = Font( 15.0)

        setOnAction {
            model.setCreateBoardMenu(true)
        }
    }

    private val logoutButton = Button("Logout").apply {
        //TODO: Add styles in CSS sheet with additional hover properties
        style = "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54)"
        textFill = Color.INDIANRED
        font = Font(15.0)

        setOnAction {
            model.logout()
        }
    }

    override fun updateView() {
        boardArea.children.clear()

        model.getBoards().forEachIndexed { index, board ->
            boardArea.children.add(Label(board.name).apply {
                textFill = Color.WHITE
                padding = Insets(10.0)
                font = Font(15.0)
                alignment = Pos.CENTER_LEFT
                prefWidth = 150.0

                //TODO: Add styles in CSS sheet with additional hover properties
                style = if (board == model.getCurrentBoard()) {
                    "-fx-background-color: rgb(104, 104, 104);\n" +
                    " -fx-pref-width: 150.0;\n -fx-alignment: CENTER_LEFT;\n" +
                    " -fx-border-radius: 20px"
                } else {
                    "-fx-background-color: rgb(52, 52, 54);\n" +
                    " -fx-pref-width: 150.0;\n -fx-alignment: CENTER_LEFT;\n" +
                    " -fx-border-radius: 20px"
                }

                setOnMouseClicked {
                    model.updateCurrentBoard(index)
                }

            })
        }


        // display new board area, which is scrollable
        center = ScrollPane(boardArea).apply {
            style = "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54)"
            isFitToWidth = true
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
        top = username
        bottom = VBox(newBoardButton, logoutButton)
        model.addView(this)

        app.addHotkey(KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)) {
            model.setCreateBoardMenu(true)
        }
        app.addHotkey(KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN)) {
            boardArea.children.firstOrNull()?.requestFocus()
        }
    }
}
