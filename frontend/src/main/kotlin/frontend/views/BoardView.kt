package frontend.views

import frontend.interfaces.IView
import frontend.Model
import frontend.services.ItemService
import javafx.geometry.*
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.text.Font
import frontend.utils.ApplicationState
import javafx.collections.FXCollections
import javafx.scene.control.ListView
import javafx.scene.paint.Color

class BoardView(private val model: Model): VBox(), IView {

    private var createToDoHeader = Label("Create To-Do item:")
    private var createToDoRowView = CreateToDoRowView(model)
    private var myToDosHeader = Label("My To-Dos:")

    private var dragFromIndex = -1
    private var dragToIndex = -1

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

        createToDoHeader.padding = Insets(0.0, 0.0, 10.0, 0.0)
        createToDoHeader.font = Font(16.0)
        children.add(createToDoHeader)

        children.add(createToDoRowView)

        myToDosHeader.padding = Insets(20.0, 0.0, 10.0, 0.0)
        myToDosHeader.font = Font("Regular", 16.0)
        children.add(myToDosHeader)

        if (model.getApplicationState() == ApplicationState.Ready) {
            noteList.items.clear()
            for ((index, item) in model.getItems(model.getCurrentBoard().id).withIndex()) {
                noteList.items.add(ToDoRowView(item).apply{
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
                            model.changeOrder(dragFromIndex, dragToIndex)
                            dragFromIndex = -1
                            dragToIndex = -1
                        }
                    }
                })
            }
            children.add(noteList)
        } else {
            children.add(Label("Loading..."))
        }

        isVisible = !model.showCreateBoard
    }

    init {
        HBox.setHgrow(this, Priority.ALWAYS)
        VBox.setVgrow(this, Priority.ALWAYS)
        padding = Insets(10.0)
        model.addView(this)
    }
}
