import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import models.Item
import views.PriorityTagView
import java.time.format.DateTimeFormatter

class ToDoRowView(item: Item): VBox() {

    private val gridPane =  GridPane()
    private val completedCheckBox = CheckBox()
    private val titleLabel = Label()
    private val dueDateLabel = Label()
    private val tagLabel = Label()
    private val assignedToLabel = Label()

    init {
        gridPane.hgap = 10.0
        gridPane.vgap = 5.0

        completedCheckBox.isSelected = item.done
        gridPane.add(completedCheckBox, 1, 0)

        titleLabel.text = item.text
        titleLabel.minWidth = 200.0
        gridPane.add(titleLabel, 2, 0)

        val formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy")
        dueDateLabel.text = item.dueDate?.format(formatter)
        gridPane.add(dueDateLabel, 3, 0)

        gridPane.add(PriorityTagView(item.priority), 4, 0)

        // TODO: right now only handling one label
        tagLabel.text = if (item.labels.size == 0) "" else item.labels.first().value
        gridPane.add(tagLabel, 5, 0)

        assignedToLabel.text = "Me"
        gridPane.add(assignedToLabel, 6, 0)

        padding = Insets(5.0, 0.0, 5.0, 0.0)
        children.add(gridPane)
    }
}