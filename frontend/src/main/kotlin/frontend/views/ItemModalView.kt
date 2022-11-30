package frontend.views

import frontend.Main
import frontend.Model
import frontend.app
import frontend.services.AuthService
import frontend.services.BoardService
import frontend.services.ItemService
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.FileChooser
import javafx.util.Duration
import javafx.util.StringConverter
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import models.Attachment
import models.Item
import models.Label
import models.User
import java.awt.Desktop
import java.io.File

class ItemModalView(private val model: Model, private val inputItem: Item?): VBox(), CoroutineScope {
    override val coroutineContext = Dispatchers.JavaFx

    private val item = inputItem?: Item(
        boardId = model.currentBoard.value.id,
        owner = AuthService.user
    )

    private val heading = Label().apply {
        text = if(inputItem == null){ "Create Item" } else{ "Edit Item" }
        font = Font(20.0)
    }

    private val deleteButton = Button().apply {
        id = "toggle"
        val deleteImage = Image(
            Main::class.java.getResource("/icons/ui_icons/delete.png").toExternalForm(),
            24.0,
            24.0,
            false,
            false
        )
        translateY = -10.0
        graphic = ImageView(deleteImage)
        setOnAction {
            model.items.remove(inputItem)
            model.additionalModalView.set("")
            launch {
                ItemService.deleteItem(model.currentBoard.value.id, inputItem!!.id)
            }
        }
    }

    private val header = HBox().apply {
         val spacer = Pane().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        }
        children.add(heading)
        if(inputItem != null) { children.addAll(spacer, deleteButton) }
    }

    private val titleInput = TextField().apply {
        id = "input"
        promptText = "Title"
        text = item.title
        prefWidth = 250.0
    }

    private val datePicker = DatePicker().apply {
        promptText = "Due Date"
        prefWidth = 140.0

        if(inputItem != null && item.dueDate != null){
            value = item.dueDate!!.toLocalDate()
        }
    }

    private val titleAndDateBox = HBox(titleInput, datePicker).apply {
        spacing = 10.0
    }

    private val labelsComboBox = ComboBox<Label>().apply {
        prefWidth = 140.0
        promptText = "Pick/enter label"
        isEditable = true
        if(inputItem != null){
            items.add(Label("")) //empty option to remove label from an item
        }
        items.addAll(model.currentBoard.value.labels)

        converter = object : StringConverter<Label>() {
            override fun fromString(str: String) = Label(str)
            override fun toString(obj: Label?) = obj?.value
        }

        model.currentBoard.addListener { _, _, board ->
            items.setAll(board.labels)
        }

        if(inputItem != null && item.labels.size > 0){
            value = item.labels.first()
        }
    }

    private val priorityComboBox = ComboBox<Int>().apply {
        promptText = "Priority"

        items.setAll(0, 1, 2)

        if(inputItem != null) { selectionModel.clearAndSelect(item.priority) }

        converter = object : StringConverter<Int>() {
            override fun toString(obj: Int?) = when (obj) {
                0 -> "Low"
                1 -> "Medium"
                2 -> "High"
                else -> "Low"
            }

            override fun fromString(str: String?) = when (str) {
                "Low" -> 0
                "Medium" -> 1
                "High" -> 2
                else -> 0
            }
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

    private val dropdowns = HBox().apply {
        spacing = 10.0
        children.addAll(priorityComboBox, labelsComboBox, assignedToComboBox)
    }

    private val descInput = TextArea().apply {
        promptText = "Description..."
        minHeight = 100.0
        isWrapText = true

        text = item.description
    }

    private val attachmentGroup:VBox = VBox().apply {
        spacing = 5.0
    }

    private val attachmentList = emptySet<Attachment>().toMutableSet()

    private val attachButton = Button("Attach").apply {
        setOnAction {
            val selectedFiles = FileChooser().showOpenMultipleDialog(app.stage)
            if (selectedFiles != null) {
                for (file in selectedFiles) {
                    val attachment = Attachment(file.name)
                    attachmentList.add(attachment)

                    val openFile = Button("${file.name}").apply {
                        setOnAction {
                        }
                    }

                    val deleteFile = Button("x").apply {
                        setOnAction {
                            attachmentList.remove(attachment)
                            attachmentGroup.children.remove(HBox(openFile, this))
                        }
                    }

                    val fileRow = HBox(openFile, deleteFile).apply {
                        id = "attachment"
                    }
                    attachmentGroup.children.add(fileRow)
                }
            }
        }
    }

    private val saveButton = Button("Save").apply {
        id = "save"
        minWidth = 75.0
        prefWidth = 75.0
        isDefaultButton = true

        setOnAction {
            if (labelsComboBox.value != null && !labelsComboBox.value.value.isNullOrBlank()) {
                val updateBoard = model.currentBoard.value.copy()
                if (updateBoard.labels.add(labelsComboBox.value)) {
                    launch {
                        model.currentBoard.set(updateBoard)
                    }
                }
            }
            updateTodo()
            model.additionalModalView.set("")
        }
    }

    private val cancelButton = Button("Cancel").apply{
        background = Background(BackgroundFill(Color.INDIANRED, CornerRadii(2.5), null))
        setOnAction {
            model.additionalModalView.set("")
        }
        isCancelButton = true
    }


    private val buttons = HBox().apply{
        val spacer = Pane().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        }
        children.addAll(spacer, cancelButton, saveButton)
        spacing = 10.0
    }

     private fun updateTodo(){
         val labels = if (labelsComboBox.value != null) {
             mutableSetOf(labelsComboBox.value)
         } else {
             mutableSetOf()
         }

         val updatedItem = item.copy(
             title = titleInput.text,
             dueDate = if (datePicker.value == null) {
                 null
             } else {
                 datePicker.value.atStartOfDay()
             },
             boardId = model.currentBoard.value.id,
             labels = labels,
             priority = priorityComboBox.value?: 0,
             owner = assignedToComboBox.value ?: AuthService.user,
             description = descInput.text,
             attachments = attachmentList.toMutableSet()
         )


         if(inputItem == null) {
             model.items.add(updatedItem)
             launch {
                 ItemService.addItem(model.currentBoard.value.id, updatedItem)
             }
         }
         else{
             val item = model.items.indexOf(inputItem)
             model.items[item] = updatedItem
             launch {
                 ItemService.updateItem(model.currentBoard.value.id, updatedItem)
             }
         }
     }


    init {
        padding = Insets(20.0)
        id = "modal"
        spacing = 20.0
        maxWidth = 400.0
        maxHeight = 450.0
        background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
        children.addAll(header, titleAndDateBox, dropdowns, descInput, attachmentGroup, buttons)
        attachmentGroup.children.add(attachButton)
    }
}