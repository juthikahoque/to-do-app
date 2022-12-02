package frontend.views

import frontend.Model
import frontend.services.ItemService
import frontend.utils.Actions
import frontend.utils.UndoRedoManager
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label

import javafx.scene.input.MouseButton
import javafx.scene.layout.*
import javafx.scene.paint.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import models.Item
import java.time.format.DateTimeFormatter
import kotlin.coroutines.CoroutineContext

class ToDoRowView(private val item: Item, model: Model) : VBox(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.JavaFx

    private val completedCheckBox = CheckBox().apply {
        isSelected = item.done
        setOnAction {
            UndoRedoManager.handleAction(
                Actions.UPDATE_ITEM,
                model.items,
                model.boards,
                null
            )
            val idx = model.items.indexOf(item)
            val new = item.copy(done = isSelected)
            model.items[idx] = new
            launch {
                ItemService.updateItem(item.boardId, new)
            }
        }
    }
    private val titleLabel = Label(item.title).apply {
        minWidth = 200.0
        maxWidth = 600.0
    }
    private val dueDateLabel = Label().apply {
        val formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy")
        text = item.dueDate?.format(formatter)
    }
    private val labelVal = if (item.labels.size == 0) "" else item.labels.first().value

    private val tags = if (labelVal != "") {
        VBox(LabelView(labelVal), PriorityTagView(item.priority)).apply {
            spacing = 5.0
        }
    } else {
        VBox(PriorityTagView(item.priority), Label()).apply {
            translateY = 10.0
        }
    }

    private val ownerLabel = Label(item.owner.name)

    private val editButton = Button().apply{
        id = "edit-button"
        background = Background(BackgroundFill(Color.TRANSPARENT, null, null))
        setOnAction {
            model.currentItem.set(item)
            model.additionalModalView.set(Presenter.editItem)
        }
    }

    private val gridPane = GridPane().apply {
        hgap = 10.0
        vgap = 5.0
        add(completedCheckBox, 1, 0)
        add(titleLabel, 2, 0)
        add(dueDateLabel, 3, 0)
        add(tags, 4, 0)
        add(ownerLabel, 5, 0)
        add(editButton, 6, 0)

        val alwaysGrow = ColumnConstraints().apply {
            hgrow = Priority.ALWAYS
        }

        val neverGrow = ColumnConstraints().apply {
            hgrow = Priority.NEVER
        }

        columnConstraints.addAll(
            neverGrow,  //completedCheckBox
            neverGrow,  //titleLabel
            alwaysGrow, //dueDateLabel
            neverGrow,  //tags
            neverGrow,  //assignedToLabel
            neverGrow,  //edit button
        )
    }

    init {
        padding = Insets(5.0, 0.0, 5.0, 0.0)
        children.add(gridPane)
        setOnMouseClicked {
            if (it.button == MouseButton.PRIMARY && it.clickCount == 2) {
                model.currentItem.set(item)
                model.additionalModalView.set(Presenter.editItem)
            }
        }
    }
}