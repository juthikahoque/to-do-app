package frontend.views

import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.text.TextAlignment
import models.Item
import java.time.format.DateTimeFormatter

class ToDoRowView(item: Item): VBox() {

    private val completedCheckBox = CheckBox().apply {
        isSelected = item.done
    }
    private val titleLabel = Label(item.text).apply{
        minWidth = 200.0
        maxWidth = 600.0
    }
    private val dueDateLabel = Label().apply {
        val formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy")
        text = item.dueDate?.format(formatter)
    }
    private val tagLabel = Label().apply {
        // TODO: right now only handling one label
        text = if (item.labels.size == 0) "" else item.labels.first().value
    }
    private val assignedToLabel = Label("Me")


    private val gridPane =  GridPane().apply {
        hgap = 10.0
        vgap = 5.0
        add(completedCheckBox, 1, 0)
        add(titleLabel, 2, 0)
        add(dueDateLabel, 3, 0)
        add(PriorityTagView(item.priority), 4, 0)
        add(tagLabel, 5, 0)
        add(assignedToLabel, 6, 0)

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
    }
}