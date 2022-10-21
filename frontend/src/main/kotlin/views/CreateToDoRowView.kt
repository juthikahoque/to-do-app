import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import java.time.LocalDate

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
            val date = datePicker.value as LocalDate
            //val boardUUID = model.getCurrentBoard().id
            val labels = mutableSetOf(labelsComboBox.value)
            val priority = priorityChoiceBox.selectionModel.selectedItem

            // TODO: call create to do service
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