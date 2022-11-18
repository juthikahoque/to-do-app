package frontend.views

import frontend.Model
import frontend.app
import frontend.interfaces.IView
import frontend.utils.ApplicationState
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import models.Item
import java.util.*
import kotlin.math.min

class BoardView(private val model: Model) : VBox(), IView {

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

    private var noteList = ListView<ToDoRowView>().apply {
        background = Background(BackgroundFill(
            Color.TRANSPARENT,
            CornerRadii.EMPTY, Insets.EMPTY
        ))
        HBox.setHgrow(this, Priority.ALWAYS)
        setVgrow(this, Priority.ALWAYS)
    }

    override fun updateView() {
        children.clear()

        children.addAll(createToDoHeader, createToDoRowView, myToDosHeader)

        noteList.items.clear()

        val allNotes = model.getCurrentItems()

        if (model.getApplicationState() == ApplicationState.Ready) {
            for (item in allNotes) {
                noteList.items.add(ToDoRowView(item, model).apply {
                    setOnDragDetected {
                        // don't allow re-ordering on all board
                        if (!model.customOrderEnabled()) return@setOnDragDetected

                        val index = noteList.items.indexOf(this)
                        startFullDrag()
                        dragFromIndex = index
                        dragToIndex = index

                        this.completedCheckBox.padding = Insets(0.0, 0.0, 0.0, 10.0)
                    }

                    setOnMouseDragOver {
                        val index = noteList.items.indexOf(this)
                        if (index == dragToIndex || dragFromIndex == -1) return@setOnMouseDragOver
                        // move all item between dragToIndex and index
                        val old = noteList.items[dragToIndex]

                        val dir = if (dragToIndex > index) -1 else 1
                        while (dragToIndex != index) {
                            noteList.items[dragToIndex] = noteList.items[dragToIndex + dir]
                            dragToIndex += dir
                        }

                        noteList.items[index] = old
                        dragToIndex = index

                        noteList.selectionModel.select(index)
                    }
                })
            }
            // cancel re-ordering if mouse leaves noteList
            noteList.setOnMouseExited {
                if (dragFromIndex != -1 && dragToIndex != dragFromIndex) {
                    val old = noteList.items[dragToIndex]
                    old.completedCheckBox.padding = Insets(0.0)
                    val dir = if (dragToIndex > dragFromIndex) -1 else 1
                    while (dragToIndex != dragFromIndex) {
                        noteList.items[dragToIndex] = noteList.items[dragToIndex + dir]
                        dragToIndex += dir
                    }
                    noteList.items[dragFromIndex] = old
                    noteList.selectionModel.select(dragFromIndex)
                    // reset drag
                    dragFromIndex = -1
                }
            }

            noteList.setOnMouseDragReleased {
                noteList.items[dragToIndex].completedCheckBox.padding = Insets(0.0)
                if (dragFromIndex != dragToIndex) {
                    selectIdx = dragToIndex
                    model.changeItemOrder(dragFromIndex, dragToIndex)
                }
                dragFromIndex = -1
            }

            children.add(noteList)

            noteList.selectionModel.select(min(selectIdx, noteList.items.size - 1))
        } else {
            children.add(Label("Loading..."))
        }
    }

    init {
        HBox.setHgrow(this, Priority.ALWAYS)
        VBox.setVgrow(this, Priority.ALWAYS)
        padding = Insets(10.0)
        model.addView(this)

        app.addHotkey(KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN)) {
            noteList.requestFocus()
            noteList.selectionModel.select(0)
        }

        app.addHotkey(KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN)) {
            if (noteList.isFocused) {
                val item = noteList.focusModel.focusedItem.item
                selectIdx = noteList.focusModel.focusedIndex
                model.updateItem(item.copy(done = !item.done))
            }
        }

        app.addHotkey(KeyCodeCombination(KeyCode.DELETE)) {
            if (noteList.isFocused) {
                val item = noteList.selectionModel.selectedItem.item
                selectIdx = noteList.selectionModel.selectedIndex
                model.deleteItem(item)
            }
        }

        app.addHotkey(KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN)) {
            if (noteList.isFocused) {
                copiedItem = noteList.selectionModel.selectedItem.item.copy()
            }
        }

        app.addHotkey(KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN)) {
            val item = copiedItem
            if (noteList.isFocused && item != null) {
                model.addToDoItem(item.copy(
                    boardId = model.getCurrentBoard().id,
                    id = UUID.randomUUID()
                ))
            }
        }
    }
}
