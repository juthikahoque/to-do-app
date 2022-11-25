package frontend.views

import frontend.Model
import frontend.app
import frontend.services.ItemService
import frontend.utils.ApplicationState
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
    private var selectIdx = 0
    private var copiedItem: Item? = null
    private var isCut: Boolean = false

    private val itemList = ListView(model.items).apply {
        background = Background(
            BackgroundFill(
                Color.TRANSPARENT,
                CornerRadii.EMPTY, Insets.EMPTY
            )
        )
        HBox.setHgrow(this, Priority.ALWAYS)
        setVgrow(this, Priority.ALWAYS)

        setCellFactory {
            object : ListCell<Item?>() {
                override fun updateItem(item: Item?, empty: Boolean) {
                    super.updateItem(item, empty)
                    graphic = if (item != null) {
                        ToDoRowView(item, model)
                    } else {
                        null
                    }

                    padding = if (dragFromIndex != -1 && isSelected) {
                        Insets(1.0, 1.0, 1.0, 15.0)
                    } else {
                        Insets(1.0)
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
            if (dragFromIndex != dragToIndex) {
                selectIdx = dragToIndex
                launch {
                    ItemService.orderItem(model.currentBoard.value.id, dragFromIndex, dragToIndex)
                    model.updateItems()
                    dragFromIndex = -1
                }
            }
        }

        model.currentItem.bind(selectionModel.selectedItemProperty())
    }

    private fun copy(cut: Boolean = false) {
        if (itemList.isFocused) {
            copiedItem = itemList.selectionModel.selectedItem.copy()
            isCut = cut
        }
    }
    private fun paste() {
        val item = copiedItem
        if (item != null) {
            val newItem = item.copy(
                boardId = model.currentBoard.value.id,
                id = UUID.randomUUID()
            )
            model.items.add(newItem)
            launch {
                ItemService.addItem(model.currentBoard.value.id, newItem)
                if (isCut) {
                    model.items.remove(item)
                    ItemService.deleteItem(item.boardId, item.id)
                    isCut = false
                }
            }
        }
    }

    private fun delete() {
        if (itemList.isFocused) {
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

        children.addAll(createToDoHeader, createToDoRowView, myToDosHeader)
        model.applicationState.addListener { _, _, _ -> updateApplicationState() }
        updateApplicationState()

        app.addHotkey(KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN)) {
            itemList.requestFocus()
            itemList.selectionModel.select(0)
        }

        app.addHotkey(KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN)) {
            if (itemList.isFocused) {
                val item = itemList.focusModel.focusedItem
                selectIdx = itemList.focusModel.focusedIndex
                val new = item.copy(done = !item.done)
                itemList.items[selectIdx] = new
                launch {
                    ItemService.updateItem(model.currentBoard.value.id, new)
                }
            }
        }

        app.addHotkey(KeyCodeCombination(KeyCode.DELETE)) {
            delete()
        }

        app.addHotkey(KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN)) {
            copy()
        }

        app.addHotkey(KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN)) {
            paste()
        }

        app.addHotkey(KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN)) {
            copy(true)
        }
    }
}
