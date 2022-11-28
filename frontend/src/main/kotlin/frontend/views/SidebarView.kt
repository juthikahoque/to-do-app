package frontend.views

import frontend.Model
import frontend.app
import frontend.services.AuthService
import frontend.services.BoardService
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.image.Image
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
import javafx.scene.paint.ImagePattern
import javafx.scene.shape.Circle
import javafx.scene.text.Font
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import models.Board
import kotlin.coroutines.CoroutineContext


/* The view for the sidebar which includes
    - The current user's username
    - A scrollable list of boards available to the user
    - Buttons that allow for new boards to be created and the user to logout
 */
class SidebarView(private val model: Model) : BorderPane(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.JavaFx

    private val userDetails = VBox().apply {
        val profilePic = Image(AuthService.firebaseUser?.photoUrl)
        val username = Label(AuthService.user.name).apply {
            textFill = Color.LIGHTBLUE
            font = Font(20.0)
            id = "name"
        }
        val pictureCircle = Circle(40.0).apply {
            fill = ImagePattern(profilePic)
            translateX = 35.0
        }
        spacing = 10.0
        children.addAll(pictureCircle, username)
    }

    private var dragFromIndex = -1
    private var dragToIndex = -1
    private var selectIdx = 0

    //List of available boards
    private val boardList = ListView(model.boards).apply {
        id = "boardView"

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

                override fun updateItem(item: Board?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item != null) {
                        button.text = item.name
                        graphic = button
                    } else {
                        graphic = null
                    }

                    id = "currentBoard"

                    button.prefWidthProperty().bind(widthProperty())

                    padding = if (dragFromIndex != -1 && isSelected) {
                        Insets(1.0, 1.0, 1.0, 15.0)
                    } else {
                        Insets(1.0)
                    }

                    setOnDragDetected {
                        if (item == null || item == model.allBoard) return@setOnDragDetected
                        val index = items.indexOf(item)
                        startFullDrag()
                        dragFromIndex = index
                        dragToIndex = index
                    }

                    setOnMouseDragOver {
                        if (item == null || item == model.allBoard) return@setOnMouseDragOver
                        val index = items.indexOf(item)
                        if (index == dragToIndex || dragFromIndex == -1) return@setOnMouseDragOver
                        // move all item between dragToIndex and index
                        val old = items[dragToIndex]

                        val dir = if (dragToIndex > index) -1 else 1
                        while (dragToIndex != index) {
                            items[dragToIndex] = items[dragToIndex + dir]
                            dragToIndex += dir
                        }

                        items[index] = old
                        dragToIndex = index

                        selectionModel.select(index)
                    }
                }
            }
        }
        // re-order on drag release
        setOnMouseDragReleased {
            if (dragFromIndex != -1 && dragFromIndex != dragToIndex) {
                selectIdx = dragToIndex
                launch {
                    // due to first item being all board, 2nd item has index 0,
                    // indexes are 1 higher than they are supposed to be
                    BoardService.orderBoard(dragFromIndex - 1, dragToIndex - 1)
                    model.updateBoards()
                    dragFromIndex = -1
                }
            }
        }
        // cancel re-ordering if mouse leaves boardList
        setOnMouseExited {
            if (dragFromIndex != -1) {
                if (dragToIndex != dragFromIndex) {
                    val old = items[dragToIndex]
                    val dir = if (dragToIndex > dragFromIndex) -1 else 1
                    while (dragToIndex != dragFromIndex) {
                        items[dragToIndex] = items[dragToIndex + dir]
                        dragToIndex += dir
                    }
                    items[dragFromIndex] = old
                    selectionModel.select(dragFromIndex)
                } else {
                    refresh()
                }
                // reset drag
                dragFromIndex = -1
            }
        }
    }

    private val newBoardButton = Button("Create Board").apply {
        id = "create"
        font = Font(15.0)

        setOnAction {
            model.additionalModalView.set(Presenter.newBoard)
        }
    }

    private val logoutButton = Button("Logout").apply {
        id = "logout"
        font = Font(15.0)

        setOnAction {
            model.logout()
        }
    }

    init {
        padding = Insets(10.0)
        minWidth = 175.0
        background = Background(BackgroundFill(Color.web("#343436"), null, null))
        top = userDetails
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
