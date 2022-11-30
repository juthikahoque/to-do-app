package frontend.views

import frontend.Model
import frontend.app
import frontend.services.AuthService
import frontend.services.BoardService
import frontend.services.ItemService
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.util.StringConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import models.Item
import models.Label
import models.User
import java.time.LocalDate

class CreateToDoRowView(private val model: Model) : HBox(), CoroutineScope {
    override val coroutineContext = Dispatchers.JavaFx

    private val titleInput = TextField().apply {
        id = "input"
    }
    private val datePicker = DatePicker()

    private val priorityChoiceBox = ChoiceBox<Int>().apply {
        items.setAll(0, 1, 2)
        selectionModel.selectFirst()

        converter = object : StringConverter<Int>() {
            override fun toString(obj: Int?) = when (obj) {
                0 -> "low"
                1 -> "medium"
                2 -> "high"
                else -> "low"
            }

            override fun fromString(str: String?) = when (str) {
                "low" -> 0
                "medium" -> 1
                "high" -> 2
                else -> 0
            }
        }
    }

    private val labelsComboBox = ComboBox<Label>().apply {
        promptText = "Select label(s)"
        isEditable = true

        items.setAll(model.currentBoard.value.labels)

        converter = object : StringConverter<Label>() {
            override fun fromString(str: String) = Label(str)
            override fun toString(obj: Label?) = obj?.value
        }

        model.currentBoard.addListener { _, _, board ->
            items.setAll(board.labels)
        }
    }

    private val assignedToComboBox = ComboBox<User>().apply {
        promptText = "Assign to..."
        isEditable = false
        items.setAll(model.currentBoard.value.users)

        converter = object : StringConverter<User>() {
            override fun fromString(str: String) = model.currentBoard.value.users.find { it.name == str }
            override fun toString(obj: User?) = obj?.name
        }

        model.currentBoard.addListener { _, _, board ->
            items.setAll(board.users)
        }
    }

    private val createButton = Button("Create").apply {
        minWidth = 75.0
        prefWidth = 75.0

        setOnAction {
            if (labelsComboBox.value != null) {
                val currentList = model.currentBoard.value.labels
                if (!currentList.contains(labelsComboBox.value)) {
                    currentList.add(labelsComboBox.value)
                    launch {
                        BoardService.updateBoard(model.currentBoard.value.copy(labels = currentList))
                    }
                }
            }
            createTodo()
        }

        defaultButtonProperty().bind(
            focusedProperty()
                .or(titleInput.focusedProperty())
                .or(datePicker.focusedProperty())
                .or(priorityChoiceBox.focusedProperty())
                .or(labelsComboBox.focusedProperty())
                .or(assignedToComboBox.focusedProperty())
        )
    }

    private fun createTodo() {
        val labels = if (labelsComboBox.value != null) {
            mutableSetOf(labelsComboBox.value)
        } else {
            mutableSetOf()
        }

        val todo = Item(
            title = titleInput.text,
            dueDate = datePicker.value.atStartOfDay(),
            boardId = model.currentBoard.value.id,
            labels = labels,
            priority = priorityChoiceBox.value,
            owner = assignedToComboBox.value ?: AuthService.user,
        )
        model.items.add(todo)
        launch {
            ItemService.addItem(todo.boardId, todo)
        }
        titleInput.requestFocus()
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

        app.addHotkey(KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN)) {
            titleInput.requestFocus()
        }
    }
}