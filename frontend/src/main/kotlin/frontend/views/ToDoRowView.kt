package frontend.views

import frontend.Model
import frontend.services.ItemService
import javafx.geometry.Insets
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.input.MouseButton
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
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
        VBox(PriorityTagView(item.priority), LabelView(labelVal)).apply {
            spacing = 5.0
        }
    } else {
        VBox(PriorityTagView(item.priority), Label()).apply {
            translateY = 5.0
        }
    }

    private val ownerLabel = Label(item.owner.name)

    private val gridPane = GridPane().apply {
        hgap = 10.0
        vgap = 5.0
        add(completedCheckBox, 1, 0)
        add(titleLabel, 2, 0)
        add(dueDateLabel, 3, 0)
        //add(PriorityTagView(item.priority, true), 4, 0)
        add(tags, 4, 0)
        add(ownerLabel, 5, 0)

        val alwaysGrow = ColumnConstraints().apply {
            hgrow = Priority.ALWAYS
        }

        val neverGrow = ColumnConstraints().apply {
            hgrow = Priority.NEVER
        }

        columnConstraints.addAll(
            neverGrow, //completedCheckBox
            neverGrow, //titleLabel
            alwaysGrow, //dueDateLabel
            neverGrow, //PriorityTagView
            neverGrow, //tagLabel
            neverGrow //assignedToLabel
        )
    }

    init {
        padding = Insets(5.0, 0.0, 5.0, 0.0)
        children.add(gridPane)
        setOnMouseClicked {
            if (it.button == MouseButton.PRIMARY) {
                model.additionalModalView.set(Presenter.editItem)
            }
        }
    }
}