package frontend.views

import frontend.Model
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ToggleButton
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color

//View for the sorting by due date, labels and priorities
class SortView(private val model: Model) : VBox() {
    private val sortChoice = ChoiceBox<String>().apply {
        items.add("Due Date")
        items.add("Priority")
        items.add("Labels")
        value = ""

        setOnAction {
            val sortBy = when (value) {
                "Due Date" -> "dueDate"
                "Priority" -> "priority"
                "Labels" -> "label"
                else -> ""
            }
            model.sort.set(sortBy)
        }
    }

    private val sortOrderButton = Button().apply {
        val ascImage = Image("/icons/sort_icons/sort-up.png", 15.0, 15.0, true, false)
        val descImage = Image("/icons/sort_icons/sort-down.png", 15.0, 15.0, true, false)

        val ascImageView = ImageView(ascImage)
        val descImageView = ImageView((descImage))

        graphic = descImageView
        background = Background(BackgroundFill(Color.TRANSPARENT, null, null))
        setOnAction {
            if (graphic == ascImageView) {
                graphic = descImageView
                model.order.set("DESC")
            } else {
                graphic = ascImageView
                model.order.set("ASC")
            }
        }
    }

    private val options = HBox(sortChoice, sortOrderButton)

    private val sortButton = ToggleButton("Sort").apply {
        background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii(5.0), Insets(0.0)))
        prefWidth = 125.0
        setOnAction {
            showOptions(isSelected)
            background = if (isSelected) {
                val sortBy = when (sortChoice.value) {
                    "Due Date" -> "dueDate"
                    "Priority" -> "priority"
                    "Labels" -> "label"
                    else -> ""
                }
                model.sort.set(sortBy)

                Background(BackgroundFill(Color.DARKGRAY, CornerRadii(5.0), Insets(0.0)))
            } else {
                model.sort.set("")
                Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii(5.0), Insets(0.0)))
            }
        }
    }

    private fun showOptions(show: Boolean) {
        if (show) children.add(options) else children.remove(options)
    }

    init {
        spacing = 5.0
        children.addAll(sortButton)
    }
}
