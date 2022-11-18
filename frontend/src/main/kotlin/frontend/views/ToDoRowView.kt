package frontend.views

import frontend.Model
import javafx.geometry.Insets
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import models.Item
import java.time.format.DateTimeFormatter

class ToDoRowView(val item: Item, model: Model): VBox() {
    val completedCheckBox = CheckBox().apply {
        isSelected = item.done
        setOnAction {
            model.updateItem(item.copy(done = isSelected))
        }
    }
    private val titleLabel = Label(item.text).apply{
        minWidth = 200.0
        maxWidth = 600.0
    }
    private val dueDateLabel = Label().apply {
        val formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy")
        text = item.dueDate?.format(formatter)
    }
    private val labelVal = if (item.labels.size == 0) "" else item.labels.first().value

    val tags = if(labelVal != ""){
        VBox(PriorityTagView(item.priority), LabelView(labelVal)).apply {
            spacing = 5.0
        }
    } else{
        VBox(PriorityTagView(item.priority), Label()).apply {
            translateY = 5.0
        }
    }

    private val assignedToLabel = Label("Me")


    private val gridPane =  GridPane().apply {
        hgap = 10.0
        vgap = 5.0
        add(completedCheckBox, 1, 0)
        add(titleLabel, 2, 0)
        add(dueDateLabel, 3, 0)
        add(tags, 4, 0)
        add(assignedToLabel, 5, 0)

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