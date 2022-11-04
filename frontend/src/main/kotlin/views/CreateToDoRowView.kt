import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import models.Item
import models.Label
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class CreateToDoRowView(private val model: Model): HBox(), IView {

    private val titleInput = TextField()
    private val datePicker = DatePicker()
    private val priorityChoiceBox = ChoiceBox<Int>()
    private val labelsComboBox = ComboBox<String>()
    private val assignedToComboBox = ComboBox<String>()
    private val createButton = Button("Create")
    private val gridPane = GridPane()

    override fun updateView() {
        isVisible = !model.showCreateBoard
        datePicker.value = LocalDate.now()
        priorityChoiceBox.selectionModel.selectFirst()
    }

    init {
        // to do title textfield
        titleInput.promptText = "To-Do Title"

        // today's date
        datePicker.value = LocalDate.now()
        datePicker.setOnAction {
            val date = datePicker.value
            println("Selected date: $date")
        }

        // choicebox for priority
        priorityChoiceBox.items.addAll(0, 1, 2)
        priorityChoiceBox.selectionModel.selectFirst()

        // combobox for labels
        labelsComboBox.promptText = "Select label(s)"
        labelsComboBox.isEditable = true
        labelsComboBox.items.add("Label 1")
        /*for (label in model.getCurrentBoard().labels) {
            labelsComboBox.items.add(label.value)
        }*/

        // combobox for assigning todo
        assignedToComboBox.promptText = "Assign to..."
        assignedToComboBox.isEditable = true
        /*for (user in model.getCurrentBoard().users) {
            // TODO: will need to accept name
            assignedToComboBox.items.add(user.toString())
        }*/

        createButton.minWidth = 75.0

        // handle create button click
        createButton.setOnMouseClicked {
            val title = titleInput.text
            val date = datePicker.value.atStartOfDay()
            val boardId = model.getCurrentBoard().id
            //val labels = Label(labelsComboBox.value)
            val priority = priorityChoiceBox.selectionModel.selectedItem

            val todo = Item(
                title,
                date,
                boardId,
                mutableSetOf<Label>(),
                priority,
            )
            model.addToDoItem(todo)
        }

        gridPane.alignment = Pos.TOP_LEFT
        gridPane.hgap = 5.0

        gridPane.add(titleInput, 1, 0)
        gridPane.add(datePicker, 2, 0)
        gridPane.add(priorityChoiceBox, 3, 0)
        gridPane.add(labelsComboBox, 4, 0)
        gridPane.add(assignedToComboBox, 5, 0)
        gridPane.add(createButton, 6, 0)

        children.add(gridPane)
        model.addView(this)
    }
}