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

class BoardView(private val model: Model): VBox(), IView {

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
    override fun updateView(){
        children.clear()

        children.addAll(createToDoHeader, createToDoRowView, myToDosHeader)
        noteList.items.clear()

        if (model.getApplicationState() == ApplicationState.Ready) {
            model.getCurrentItems().forEachIndexed { index, item ->
                noteList.items.add(ToDoRowView(item, model).apply{
                    if (model.getCurrentBoard().name != "All") {
                        setOnDragDetected {
                            startFullDrag()
                            dragFromIndex = index
                        }

                        setOnMouseDragOver {
                            if(index != dragFromIndex){
                                item.apply {
                                    border = Border(BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT))
                                }
                                dragToIndex = index
                            }
                        }

                        setOnMouseDragExited {
                            dragToIndex = -1
                            item.apply{
                                border = Border(BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT))
                            }
                        }

                        setOnMouseDragReleased {
                            if(dragToIndex != -1){
                                model.changeItemOrder(dragFromIndex, dragToIndex)
                                dragFromIndex = -1
                                dragToIndex = -1
                            }
                        }
                    }
                })
            }
            children.add(noteList)
        } else {
            children.add(Label("Loading..."))
        }

        noteList.selectionModel.select(min(selectIdx, noteList.items.size - 1))
    }

    init {
        HBox.setHgrow(this, Priority.ALWAYS)
        VBox.setVgrow(this, Priority.ALWAYS)
        padding = Insets(10.0)
        model.addView(this)

        app.addHotkey(KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN)) {
            noteList.requestFocus()
            noteList.selectionModel.select(0)
        }

        app.addHotkey(KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN)) {
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

        app.addHotkey(KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN)) {
            if (noteList.isFocused) {
                copiedItem = noteList.selectionModel.selectedItem.item.copy()
            }
        }

        app.addHotkey(KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN)) {
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
