package frontend.views

import frontend.Model
import frontend.app
import frontend.interfaces.IView
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import models.Item
import models.Label
import java.time.LocalDate

class CreateToDoRowView(private val model: Model) : HBox(), IView {

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

    private fun createTodo() {
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
        titleInput.requestFocus()
    }

    init {
        createButton.minWidth = 75.0
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

        createButton.prefWidth = 75.0

        // handle create button click
        createButton.setOnAction {
            createTodo()
        }

        gridPane.alignment = Pos.TOP_LEFT
        gridPane.hgap = 5.0

        gridPane.add(titleInput, 1, 0)
        gridPane.add(datePicker, 2, 0)
        gridPane.add(priorityChoiceBox, 3, 0)
        gridPane.add(labelsComboBox, 4, 0)
        gridPane.add(assignedToComboBox, 5, 0)
        gridPane.add(createButton, 6, 0)

        //add a spacer to make the UI responsive
        val spacer = Pane().apply {
            setHgrow(this, Priority.ALWAYS)
        }

        children.addAll(
            titleInput,
            datePicker,
            priorityChoiceBox,
            labelsComboBox,
            assignedToComboBox,
            spacer,
            createButton
        ).apply {
            spacing = 10.0
        }
        model.addView(this)

        app.addHotkey(KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN)) {
            titleInput.requestFocus()
        }

        app.addHotkey(KeyCodeCombination(KeyCode.ENTER)) {
            if (titleInput.isFocused || datePicker.isFocused || priorityChoiceBox.isFocused ||
                labelsComboBox.isFocused || assignedToComboBox.isFocused || createButton.isFocused
            ) {
                createTodo()
            }
        }
    }
}