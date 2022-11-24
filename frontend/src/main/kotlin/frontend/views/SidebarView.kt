package frontend.views

import frontend.Model
import frontend.app
import frontend.services.AuthService
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.skin.ButtonSkin
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import models.Board
import kotlin.coroutines.CoroutineContext


/* The view for the sidebar which includes
    - The current user's username
    - A scrollable list of boards available to the user
    - Buttons that allow for new boards to be created and the user to logout
 */
class SidebarView(private val model: Model) : BorderPane(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.JavaFx

    private val username = HBox(Label().apply {
        text = AuthService.user?.displayName
        textFill = Color.LIGHTBLUE
        font = Font(20.0)
        padding = Insets(0.0, 0.0, 20.0, 0.0)
    })

    //List of available boards
    private val boardList = ListView(model.boards).apply {
        style = "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54)"

        setCellFactory {
            object : ListCell<Board?>() {
                var button = Button().apply {
                    textFill = Color.WHITE
                    padding = Insets(8.0)
                    font = Font(15.0)
                    alignment = Pos.CENTER_LEFT

                    setOnAction {
                        if (item != null) {
                            model.currentBoard.set(item)
                        }
                    }

                    skin = object : ButtonSkin(this) {
                        init {
                            consumeMouseEvents(false)
                        }
                    }
                }

                fun updateStyle(selected: Board) {
                    style = if (item == selected) {
                        """
                        -fx-background-color: rgb(104, 104, 104);
                        -fx-pref-width: 140.0;
                        -fx-alignment: CENTER_LEFT;
                        -fx-border-radius: 20px
                        """.trimIndent()
                    } else {
                        """
                        -fx-background-color: rgb(52, 52, 54);
                        -fx-pref-width: 140.0;
                        -fx-alignment: CENTER_LEFT;
                        -fx-border-radius: 20px
                        """.trimIndent()
                    }
                    button.style = style
                }

                override fun updateItem(item: Board?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item != null) {
                        button.text = item.name
                        graphic = button
                    } else {
                        graphic = null
                    }

                    //TODO: Add styles in CSS sheet with additional hover properties
                    model.currentBoard.addListener { _, _, newValue -> updateStyle(newValue) }
                    updateStyle(model.currentBoard.value)

                    button.prefWidthProperty().bind(widthProperty())

                    padding = Insets(1.0)
                }
            }
        }
    }

    private val newBoardButton = Button("Create Board").apply {
        //TODO: Add styles in CSS sheet with additional hover properties
        style = "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54)"
        textFill = Color.LIGHTGREEN
        font = Font(15.0)

        setOnAction {
            model.additionalModalView.set(Presenter.newBoard)
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

    init {
        padding = Insets(10.0)
        minWidth = 175.0
        background = Background(BackgroundFill(Color.web("#343436"), null, null))

        top = username
        center = boardList
        bottom = VBox(newBoardButton, logoutButton)

        app.addHotkey(KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)) {
            if (model.additionalModalView.value.isEmpty()) {
                model.additionalModalView.set(Presenter.newBoard)
            }
        }
        app.addHotkey(KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN)) {
            boardList.focusModel.focus(0)
        }
    }
}
