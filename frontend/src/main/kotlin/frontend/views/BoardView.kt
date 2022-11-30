package frontend.views

import frontend.Model
import frontend.app
import frontend.services.ItemService
import frontend.utils.ActionMetaData
import frontend.utils.Actions
import frontend.utils.ApplicationState
import frontend.utils.UndoRedoManager
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import models.Item
import java.util.*
import kotlin.coroutines.CoroutineContext

class BoardView(private val model: Model) : VBox(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.JavaFx

    private var createToDoHeader = Label("Create To-Do item:").apply {
        padding = Insets(0.0, 0.0, 10.0, 0.0)
        font = Font(16.0)
    }
    private var createToDoRowView = CreateToDoRowView(model)

    private var myToDosHeader = Label("My To-Dos:").apply {
        padding = Insets(20.0, 0.0, 10.0, 0.0)
        font = Font("Regular", 16.0)
    }

    private var dragFromIndex = -1
    private var dragToIndex = -1
    private var copiedItem: Item? = null

    private val itemList = ListView(model.items).apply {
        background = Background(
            BackgroundFill(
                Color.TRANSPARENT,
                CornerRadii.EMPTY, Insets.EMPTY
            )
        )
        HBox.setHgrow(this, Priority.ALWAYS)
        setVgrow(this, Priority.ALWAYS)

        model.currentItem.bind(selectionModel.selectedItemProperty())

        setCellFactory {
            object : ListCell<Item?>() {
                override fun updateItem(item: Item?, empty: Boolean) {
                    super.updateItem(item, empty)

                    graphic = if (item != null) {
                        val pad = if (dragFromIndex != -1 && isSelected) 15.0 else 0.0
                        ToDoRowView(item, model).apply {
                            padding = Insets(5.0, 0.0, 5.0, pad)
                        }
                    } else {
                        null
                    }

                    setOnDragDetected {
                        // don't allow re-ordering on all board
                        if (item == null || !model.customOrderEnabled()) return@setOnDragDetected

                        val index = items.indexOf(item)
                        startFullDrag()
                        dragFromIndex = index
                        dragToIndex = index
                    }

                    setOnMouseDragOver {
                        if (item == null) return@setOnMouseDragOver
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
                    val miNew = MenuItem("New").apply { setOnAction { model.additionalModalView.set(Presenter.editItem) } }
                    val miEdit = MenuItem("Edit").apply { setOnAction { model.additionalModalView.set(Presenter.editItem) } }
                    val miCut = MenuItem("Cut").apply { setOnAction { copy(true) } }
                    val miCopy = MenuItem("Copy").apply { setOnAction { copy() } }
                    val miPaste = MenuItem("Paste").apply { setOnAction { paste() } }
                    val miDelete = MenuItem("Delete").apply { setOnAction { delete() } }

                    if (item == null) {
                        contextMenu.items.setAll(miNew, miPaste)
                    } else {
                        contextMenu.items.setAll(miNew, miEdit, miCut, miCopy, miPaste, miDelete)
                    }
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

        setOnMouseDragReleased {
            val from = dragFromIndex
            val to = dragToIndex
            dragFromIndex = -1
            refresh()

            if (from != to && from != -1) {
                UndoRedoManager.handleAction(
                    Actions.reorderItem,
                    model.items,
                    model.boards,
                    ActionMetaData(from, to)
                )
                launch {
                    ItemService.orderItem(model.currentBoard.value.id, from, to)
                }
            }
        }

        // shortcuts for items
        setOnKeyPressed {
            val deleteCode = if (System.getProperty("os.name").lowercase().contains("mac")) {
                KeyCode.BACK_SPACE
            } else {
                KeyCode.DELETE
            }
            when (it.code) { // delete item
                deleteCode -> { delete() }
                KeyCode.ENTER -> {
                    model.additionalModalView.set(Presenter.editItem)
                }
                else -> {}
            }

            if (!it.isShortcutDown) return@setOnKeyPressed
            // everything else requires shortcut down
            when (it.code) {
                KeyCode.UP -> { // re-order up
                    reorderShortcut(-1)
                }
                KeyCode.DOWN -> {
                    reorderShortcut(1)
                }
                KeyCode.C -> { // copy
                    copy()
                }
                KeyCode.X -> { // cut
                    copy(true)
                }
                KeyCode.D -> { // mark as done
                    UndoRedoManager.handleAction(
                        Actions.updateItem,
                        model.items,
                        model.boards,
                        null
                    )
                    val item = selectionModel.selectedItem
                    val new = item.copy(done = !item.done)
                    items[selectionModel.selectedIndex] = new
                    launch {
                        ItemService.updateItem(model.currentBoard.value.id, new)
                    }
                }
                else -> {}
            }
        }
    }

    private fun reorderShortcut(dir: Int) {
        if (model.currentBoard.value == model.allBoard) return
        val idx = itemList.selectionModel.selectedIndex
        if (idx + dir < itemList.items.size && idx + dir >= 0) {
            UndoRedoManager.handleAction(
                Actions.reorderItem,
                model.items,
                model.boards,
                ActionMetaData(idx, idx + dir)
            )
            // swap
            val temp = model.items[idx]
            model.items[idx] = model.items[idx + dir]
            model.items[idx + dir] = temp
            itemList.selectionModel.select(idx + dir)

            launch {
                ItemService.orderItem(model.currentBoard.value.id, idx, idx + dir)
            }
        }
    }
    private fun copy(cut: Boolean = false) {
        if (itemList.isFocused) {
            val item = itemList.selectionModel.selectedItem.copy()
            copiedItem = item
            if (cut) {
                UndoRedoManager.handleAction(
                    Actions.deleteItem,
                    model.items,
                    model.boards,
                    null
                )
                model.items.remove(item)
                launch {
                    ItemService.deleteItem(item.boardId, item.id)
                }
            }
        }
    }
    private fun paste() {
        val item = copiedItem
        if (item != null && model.currentBoard.value != model.allBoard) {
            UndoRedoManager.handleAction(
                Actions.addItem,
                model.items,
                model.boards,
                null
            )
            val newItem = item.copy(
                boardId = model.currentBoard.value.id,
                id = UUID.randomUUID(),
                labels = item.labels.map { it }.toMutableSet(),
                attachments = item.attachments.map { it }.toMutableSet(),
            )
            model.items.add(newItem)
            launch {
                ItemService.addItem(model.currentBoard.value.id, newItem)
            }
        }
    }

    private fun delete() {
        if (itemList.isFocused) {
            UndoRedoManager.handleAction(
                Actions.deleteItem,
                model.items,
                model.boards,
                null
            )
            val item = itemList.selectionModel.selectedItem
            model.items.remove(item)
            launch {
                ItemService.deleteItem(item.boardId, item.id)
            }
        }
    }


    private fun updateApplicationState() {
        if (model.applicationState.value == ApplicationState.Ready) {
            children[2] = itemList
        } else {
            children[2] = Label("Loading...")
        }
    }

    init {
        HBox.setHgrow(this, Priority.ALWAYS)
        VBox.setVgrow(this, Priority.ALWAYS)
        padding = Insets(10.0)
        id = "borderPane"

        children.addAll(createToDoHeader, createToDoRowView, myToDosHeader)
        model.applicationState.addListener { _, _, _ -> updateApplicationState() }
        updateApplicationState()

        model.currentBoard.addListener { _, _, board ->
            createToDoHeader.isVisible = board != model.allBoard
            createToDoRowView.isVisible = board != model.allBoard
        }

        app.addHotkey(KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN)) {
            itemList.requestFocus()
            itemList.selectionModel.select(0)
        }

        // paste, can paste without focus on item list, so the shortcut is global
        app.addHotkey(KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN)) {
            paste()
        }
    }
}
