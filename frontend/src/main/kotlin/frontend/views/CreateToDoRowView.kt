package frontend.views

import frontend.Model
import frontend.app
import frontend.services.BoardService
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import models.Item
import models.Label
import java.time.LocalDate

class CreateToDoRowView(private val model: Model) : HBox(), CoroutineScope {
    override val coroutineContext = Dispatchers.JavaFx

    private val titleInput = TextField()
    private val datePicker = DatePicker()
    private val priorityChoiceBox = ChoiceBox<Int>()
    private val labelsComboBox = ComboBox<String>()
    private val assignedToComboBox = ComboBox<String>()
    private val createButton = Button("Create").apply {
        isDefaultButton = true
    }

    var labelList = model.currentBoard.value.labels

    private fun createTodo() {
        val title = titleInput.text
        val date = datePicker.value.atStartOfDay()
        val boardId = model.currentBoard.value.id
        val labels = if (labelsComboBox.value != null) {
            mutableSetOf(Label(labelsComboBox.value))
        } else {
            mutableSetOf()
        }
        val priority = priorityChoiceBox.selectionModel.selectedItem

        val todo = Item(
            title,
            date,
            boardId,
            labels,
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
        model.currentBoard.addListener { _, _, board ->
            labelsComboBox.items.setAll(board.labels.map { it.value })
        }
        //labelsComboBox.items.add("Label 1")
        //labelsComboBox.
        /*for (label in model.currentBoard.value.labels) {
            labelsComboBox.items.add(label.value)
        }*/

        // combobox for assigning todo
        assignedToComboBox.promptText = "Assign to..."
        assignedToComboBox.isEditable = true
        /*for (user in model.currentBoard.value.users) {
            // TODO: will need to accept name
            assignedToComboBox.items.add(user.toString())
        }*/

        createButton.prefWidth = 75.0

        // handle create button click
        createButton.setOnAction {
            if (labelsComboBox.value != null && labelsComboBox.value.isNotBlank()) {
                val newLabel = Label(labelsComboBox.value)
                if (!labelList.contains(newLabel)) {
                    labelList.add(newLabel)
                    launch {
                        BoardService.updateBoard(model.currentBoard.value.copy(labels = labelList))
                    }
                }
            }

            createTodo()
        }
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

        createButton.defaultButtonProperty().bind(
            titleInput.focusedProperty()
                .or(datePicker.focusedProperty())
                .or(priorityChoiceBox.focusedProperty())
                .or(labelsComboBox.focusedProperty())
                .or(assignedToComboBox.focusedProperty())
                .or(createButton.focusedProperty())
        )


        app.addHotkey(KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN)) {
            titleInput.requestFocus()
        }
    }
}