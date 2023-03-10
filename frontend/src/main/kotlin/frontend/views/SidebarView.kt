package frontend.views

import frontend.Main
import frontend.Model
import frontend.app
import frontend.services.AuthService
import frontend.services.BoardService
import frontend.utils.UndoRedoManager
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
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
import kotlin.math.max


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
            alignment = Pos.CENTER
        }
        val pictureCircle = Circle(40.0).apply {
            fill = ImagePattern(profilePic)
            alignment = Pos.CENTER
        }
        spacing = 10.0
        children.addAll(pictureCircle, username)
    }

    private var isLightMode = app.getTheme() == "light"

    private val sunImg = Image(Main::class.java.getResource("/icons/themes/sun.png")!!.toExternalForm())
    private val sunImgView = ImageView(sunImg)
    private val moonImg = Image(Main::class.java.getResource("/icons/themes/moon.png")!!.toExternalForm())
    private val moonImgView = ImageView(moonImg)

    private val themeMode = VBox(ToggleButton().apply {
        font = Font(15.0)
        id = "toggle"
        graphic = if (isLightMode) moonImgView else sunImgView
        setOnAction {
            isLightMode = !isLightMode
            graphic = if (isLightMode) moonImgView else sunImgView
            app.changeThemeMode(if (isLightMode) "light" else "dark")
        }
    }).apply {
        alignment = Pos.CENTER
    }

    private var dragFromIndex = -1
    private var dragToIndex = -1

    //List of available boards
    private val boardList = ListView(model.boards).apply {
        id = "boardView"

        selectionModel.select(1)
        selectionModel.selectedItemProperty().addListener { _, _, new ->
            if (new != null && dragFromIndex == -1) { // not in drag
                model.currentBoard.set(new)
            }
        }

        model.currentBoard.addListener { _, old, new ->
            if (new != null && old != null && old.id == new.id && old.users.size != new.users.size) {
                refresh()
            }
        }

        setCellFactory {
            object : ListCell<Board?>() {
                override fun updateItem(item: Board?, empty: Boolean) {
                    super.updateItem(item, empty)

                    graphic = if (item != null) {
                        id = "currentBoard"
                        HBox().apply {
                            spacing = 10.0
                            if (item != model.allBoard) {
                                alignment = Pos.CENTER
                            }
                            val pad = if (dragFromIndex != -1 && isSelected) 15.0 else 0.0
                            padding = Insets(5.0, 0.0, 5.0, pad)

                            if (item.users.size > 1) {
                                val img =
                                    Image(
                                        Main::class.java.getResource("/icons/ui_icons/shared-icon.png")!!.toExternalForm()
                                    )
                                val shared = ImageView(img).apply {
                                    alignment = Pos.CENTER
                                }
                                children.add(shared)
                            }

                            val name = Label(item.name).apply {
                                textFill = Color.WHITE
                                font = Font(15.0)
                            }
                            children.add(name)

                            val editButton = Button().apply {
                                id = "edit-board"
                                alignment = Pos.CENTER
                                setOnAction {
                                    selectionModel.select(item)
                                    model.additionalModalView.set(Presenter.editBoard)
                                }
                            }

                            if (item != model.allBoard) {
                                val spacer = Pane().apply {
                                    HBox.setHgrow(this, Priority.ALWAYS)
                                }
                                children.addAll(spacer, editButton)
                            }
                        }

                    } else {
                        id = ""
                        null
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

                    contextMenu = ContextMenu()
                    val miNew = MenuItem("New").apply {
                        setOnAction { model.additionalModalView.set(Presenter.newBoard) }
                    }
                    val miEdit = MenuItem("Edit").apply {
                        setOnAction { model.additionalModalView.set(Presenter.editBoard) }
                    }
                    val miDelete = MenuItem("Delete").apply {
                        setOnAction {
                            val item = selectionModel.selectedItem
                            model.boards.remove(item)
                            launch {
                                BoardService.deleteBoard(item.id)
                            }
                        }
                    }
                    val miUndo = MenuItem("Undo").apply {
                        setOnAction { UndoRedoManager.handleUndo() }
                    }
                    val miRedo = MenuItem("Redo").apply {
                        setOnAction { UndoRedoManager.handleRedo() }
                    }


                    if (item == null || item == model.allBoard) {
                        contextMenu.items.setAll(miNew, miUndo, miRedo)
                    } else {
                        contextMenu.items.setAll(miNew, miEdit, miDelete, miUndo, miRedo)
                    }
                }
            }
        }
        // re-order on drag release
        setOnMouseDragReleased {
            val from = dragFromIndex
            val to = dragToIndex
            dragFromIndex = -1
            refresh()

            if (from != to && from != -1) {
                launch {
                    // due to first item being all board, 2nd item has index 0,
                    // indexes are 1 higher than they are supposed to be
                    BoardService.orderBoard(dragFromIndex - 1, dragToIndex - 1)
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

        setOnKeyPressed {
            val deleteCode = if (System.getProperty("os.name").lowercase().contains("mac")) {
                KeyCode.BACK_SPACE
            } else {
                KeyCode.DELETE
            }
            when (it.code) { // delete item
                deleteCode -> {
                    val item = selectionModel.selectedItem
                    model.boards.remove(item)
                    launch {
                        BoardService.deleteBoard(item.id)
                    }
                }

                KeyCode.ENTER -> {
                    if(selectionModel.selectedItem != model.allBoard) {
                        model.additionalModalView.set(Presenter.editBoard)
                    }
                }

                else -> {}
            }

            if (!it.isShortcutDown) return@setOnKeyPressed
            // everything else requires shortcut down
            when (it.code) {
                KeyCode.UP -> { // re-order up
                    val idx = selectionModel.selectedIndex
                    if (idx > 1) {
                        val old = items[idx]
                        items[idx] = items[idx - 1]
                        items[idx - 1] = old
                        selectionModel.select(idx - 1)

                        launch {
                            // index is off by 1 due to "All" board
                            BoardService.orderBoard(idx - 1, idx - 2)
                        }
                    }
                }

                KeyCode.DOWN -> { // re-order down
                    val idx = selectionModel.selectedIndex
                    if (idx < items.size - 1 && idx > 0) {
                        val old = items[idx]
                        items[idx] = items[idx + 1]
                        items[idx + 1] = old
                        selectionModel.select(idx + 1)

                        launch {
                            // index is off by 1 due to "All" board
                            BoardService.orderBoard(idx - 1, idx)
                        }
                    }
                }

                else -> {}
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
            AuthService.logout()
            app.changeScene("login")
        }
    }

    init {
        padding = Insets(10.0)
        prefWidth = 175.0
        maxWidth = 300.0
        id = "sidebar"
        background = Background(BackgroundFill(Color.web("#343436"), null, null))

        top = HBox(userDetails).apply {
            alignment = Pos.CENTER
            padding = Insets(10.0)
        }
        center = boardList
        val spacer = Pane().apply { HBox.setHgrow(this, Priority.ALWAYS) }
        bottom = HBox(VBox(newBoardButton, logoutButton), spacer, themeMode)


        app.addHotkey(KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)) {
            if (model.additionalModalView.value.isEmpty()) {
                model.additionalModalView.set(Presenter.newBoard)
            }
        }
        app.addHotkey(KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN)) {
            boardList.requestFocus()
        }
        // refresh all hotkey
        app.addHotkey(KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN)) {
            val selectedIdx = boardList.selectionModel.selectedIndex
            launch {
                model.updateBoards()
                boardList.selectionModel.select(max(selectedIdx, model.boards.size - 1))
                model.updateItems()
            }
        }
    }
}
