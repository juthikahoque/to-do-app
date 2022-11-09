package frontend.views

import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color

//View for the sorting by due date, labels and priorities
class SortView():VBox() {
    private val sortChoice = ChoiceBox<String>().apply {
        items.add("Due Date")
        items.add("Priority")
        items.add("Labels")
        setOnAction {
            //TODO: Backend sorting
        }
    }



    private val sortOrderButton =  Button().apply{
        val ascImage = Image("/icons/sort_icons/sort-up.png", 15.0, 15.0, true, false)
        val descImage = Image("/icons/sort_icons/sort-down.png", 15.0, 15.0, true, false)

        val ascImageView = ImageView(ascImage)
        val descImageView = ImageView((descImage))

        graphic = descImageView
        setOnAction {
            if(graphic == ascImageView){
                graphic = descImageView
                //TODO: Backend sorting
            }
            else{
                graphic = ascImageView
                //TODO: Backend sorting
            }
        }
    }

    private val options = HBox(sortChoice, sortOrderButton)

    private val sortButton = ToggleButton("Sort").apply {
        background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii(5.0), Insets(0.0)))
        prefWidth = 125.0
        setOnAction {
            showOptions(isSelected)
            background = if(isSelected){
                Background(BackgroundFill(Color.DARKGRAY, CornerRadii(5.0), Insets(0.0)))
            }
            else{
                Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii(5.0), Insets(0.0)))
            }
        }
    }

    private fun showOptions(show:Boolean){
        if(show) children.add(options) else children.remove(options)
    }

    init {
        spacing = 5.0
        children.addAll(sortButton)
    }

}