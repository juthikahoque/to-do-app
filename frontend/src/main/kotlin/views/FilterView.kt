import javafx.geometry.Insets
import javafx.scene.control.ChoiceBox
import javafx.scene.control.DatePicker
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.layout.*
import javafx.scene.paint.Color
import views.PriorityTagView


//View for the filtering by due date, labels and priorities
class FilterView():VBox(){

    private val dateFilter = HBox().apply {
        val from = DatePicker().apply { prefWidth = 125.0 }
        val toLabel = Label(" to ").apply { translateY = 5.0 }
        val to = DatePicker().apply { prefWidth = 125.0 }
        children.addAll(from, toLabel, to)
    }

    private val labelFilter = HBox().apply {
        //TODO: Get real labels
        val labels = listOf("Label1", "Label2", "Label3")
        for (label in labels) {
            children.add(ToggleButton(label))
        }
        spacing = 10.0
    }

    private val priorityGroup = HBox().apply{
        val priorityView = PriorityTagView(0)
        val labels = listOf("Low", "Medium", "High")
        for((index, label) in labels.withIndex()){
            val toggle = ToggleButton(label).apply{
                background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii(5.0), Insets(0.0)))
                val backgroundColor = priorityView.getColor(index)
                setOnAction {
                    if(isSelected){
                        background = Background(BackgroundFill(backgroundColor, CornerRadii(5.0), Insets(0.0)))
                    }
                    else{
                        background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
                    }
                }
            }
            children.add(toggle)
        }
        spacing = 10.0
    }

    private val filterChoice = ChoiceBox<String>().apply {
        items.add("Due Date")
        items.add("Priority")
        items.add("Labels")
        setOnAction {
            if(value == "Due Date"){
                updateOptions(dateFilter)
            }
            else if(value == "Priority"){
                updateOptions(priorityGroup)

            }

            else if(value == "Labels"){
                updateOptions(labelFilter)
            }
        }
    }

    private val options = HBox(filterChoice).apply {
        spacing = 5.0
    }


    private fun updateOptions(newChild: Region){
        if(options.children.size > 1){
            options.children.removeAt(1)
        }
        options.children.add(newChild)
    }

    private fun showOptions(show:Boolean){
        if(show) children.add(options) else children.remove(options)
    }

    private val filterButton = ToggleButton("Filter").apply {
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

    init {
        children.addAll(filterButton)
        spacing = 5.0
    }
}