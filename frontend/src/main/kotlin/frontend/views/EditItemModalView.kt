package frontend.views

import frontend.Model
import frontend.services.BoardService
import frontend.services.ItemService
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import kotlinx.coroutines.runBlocking
import models.Item
import java.time.LocalDate

class EditItemModalView(private val model: Model, item: Item): VBox() {

    private var selectedPriority = item.priority
    private val priorityMappings = mutableMapOf<Int, String>(
        0 to "Low",
        1 to "Medium",
        2 to "High"
    )

    private val nameInput = TextField().apply {
        text = item.text
    }

    private val datePicker = DatePicker().apply {
        value = if (item.dueDate == null) {
            LocalDate.now()
        } else {
            item.dueDate!!.toLocalDate()
        }
    }

    private val priorityChoiceHBox = HBox().apply {
        for (i in 0..2) {
            val priorityTag = if (item.priority == i) {
                PriorityTagView(i, true)
            } else {
                PriorityTagView(i, false)
            }
            priorityTag.setOnMouseClicked {
                selectedPriority = i
                updatePriorityHbox()
            }
            children.add(priorityTag)
        }
        spacing = 5.0
    }

    private fun updatePriorityHbox() {
        priorityChoiceHBox.children.clear()
        for (i in 0..2) {
            val priorityTag = if (selectedPriority == i) {
                PriorityTagView(i, true)
            } else {
                PriorityTagView(i, false)
            }
            priorityTag.setOnMouseClicked {
                selectedPriority = i
                updatePriorityHbox()
            }
            priorityChoiceHBox.children.add(priorityTag)
        }
    }


    private val saveButton = Button("Save").apply{
        background = Background(BackgroundFill(Color.LIGHTGREEN, CornerRadii(2.5), null))
        setOnAction {
            item.text = nameInput.text
            item.priority = selectedPriority
            item.dueDate = datePicker.value.atStartOfDay()

            runBlocking {
                ItemService.updateItem(item.boardId, item!!)
            }

            model.setShowEditItemModal(false, null)
            print("saved.")
        }
        isDefaultButton = true
    }

    private val cancelButton = Button("Cancel").apply{
        background = Background(BackgroundFill(Color.INDIANRED, CornerRadii(2.5), null))
        setOnAction {
            model.setShowEditItemModal(false, null)
        }
    }

    private val buttons = HBox(saveButton, cancelButton).apply{
        spacing = 10.0
    }

    init {
        padding = Insets(20.0)
        spacing = 20.0
        maxWidth = 300.0
        maxHeight = 150.0
        background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
        children.addAll(nameInput, datePicker, priorityChoiceHBox, buttons)
    }
}