package frontend.views

import frontend.Model
import frontend.interfaces.IView
import io.ktor.server.util.*
import javafx.geometry.Insets
import javafx.scene.control.ChoiceBox
import javafx.scene.control.DatePicker
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.layout.*
import javafx.scene.paint.Color
import java.time.LocalDate
import java.util.*

//View for the filtering by due date, labels and priorities
class FilterView(private val model: Model):VBox(), IView{

    private var oldestDate = LocalDate.MIN

    private var selectedDates:Pair<LocalDate, LocalDate?> = Pair(oldestDate, null)

    private val dateFilter = HBox().apply {
        val from = DatePicker().apply {
            prefWidth = 125.0
            setOnAction {
                selectedDates = selectedDates.copy(first = value)
                model.filterByDates(selectedDates)
            }

        }
        val toLabel = Label(" to ").apply { translateY = 5.0 }
        val to = DatePicker().apply {
            prefWidth = 125.0
            setOnAction {
                selectedDates = selectedDates.copy(second = value)
                model.filterByDates(selectedDates)

            }
        }
        children.addAll(from, toLabel, to)
    }

    private val selectedLabels = mutableSetOf<models.Label>()
    private val labelFilter = HBox().apply {
        //TODO: Get real labels
        for (label in model.getCurrentBoard().labels) {
            children.add(ToggleButton(label.value).apply{
                background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
                setOnAction {
                    if(isSelected){
                        background = Background(BackgroundFill(Color.LIGHTBLUE, CornerRadii(5.0), Insets(0.0)))
                        selectedLabels.add(label)
                    }
                    else{
                        background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
                        if(selectedLabels.contains(label)) { selectedLabels.remove(label) }
                    }
                    model.filterByLabels(selectedLabels)
                }
            })
        }
        spacing = 10.0
    }

    private val selectedPriorites = mutableSetOf<Int>()
    private val priorityGroup = HBox().apply{
        val priorityView = PriorityTagView(0, true)
        val labels = listOf("Low", "Medium", "High")
        for((index, label) in labels.withIndex()){
            val toggle = ToggleButton(label).apply{
                background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
                val backgroundColor = priorityView.getColor(index)
                setOnAction {
                    if(isSelected){
                        background = Background(BackgroundFill(backgroundColor, CornerRadii(5.0), Insets(0.0)))
                        selectedPriorites.add(index)
                    }
                    else{
                        background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
                        if(selectedPriorites.contains(index)) { selectedPriorites.remove(index) }
                    }
                    model.filterByPriorities(selectedPriorites)
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
                model.filterByDates(selectedDates)
            }
            else if(value == "Priority"){
                updateOptions(priorityGroup)
                model.filterByPriorities(selectedPriorites)
            }

            else if(value == "Labels"){
                updateOptions(labelFilter)
            }
        }
    }

    private val filterOptions = HBox(filterChoice).apply {
        spacing = 5.0
    }
    private fun updateOptions(newChild: Region){
        if(filterOptions.children.size > 1){
            filterOptions.children.removeAt(1)
        }
        filterOptions.children.add(newChild)
    }

    private fun showOptions(show:Boolean){
        if(show) children.add(filterOptions) else children.remove(filterOptions)
    }

    private val filterButton = ToggleButton("Filter").apply {
        background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii(5.0), Insets(0.0)))
        prefWidth = 125.0
        setOnAction {
            showOptions(isSelected)
            if(isSelected){
                background =  Background(BackgroundFill(Color.DARKGRAY, CornerRadii(5.0), Insets(0.0)))
                //re-apply the old filters
                if(filterOptions.children.contains(priorityGroup)) {
                    model.filterByPriorities(selectedPriorites)
                }

                //filtering by date
                else if(filterOptions.children.contains(dateFilter)){
                    model.filterByDates(selectedDates)
                }
            }
            else{
                background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii(5.0), Insets(0.0)))
                //switch off all filters
                model.filterByPriorities(emptySet<Int>().toMutableSet())
            }
        }
    }

    init {
        children.addAll(filterButton)
        spacing = 5.0
    }

    override fun updateView(){
        //show any new labels
        labelFilter.children.clear()
        for (label in model.getCurrentBoard().labels) {
            labelFilter.children.add(ToggleButton(label.value).apply{
                isSelected = selectedLabels.contains(label)
                background = if(isSelected) {
                    Background(BackgroundFill(Color.LIGHTBLUE, CornerRadii(5.0), Insets(0.0)))
                }else{
                    Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
                }
                setOnAction {
                    if(isSelected){
                        background = Background(BackgroundFill(Color.LIGHTBLUE, CornerRadii(5.0), Insets(0.0)))
                        selectedLabels.add(label)
                    }
                    else{
                        background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
                        if(selectedLabels.contains(label)) { selectedLabels.remove(label) }
                    }
                    model.filterByLabels(selectedLabels)
                }
            })
        }

        //filtering by priority
        if(filterOptions.children.contains(priorityGroup)) {
            model.filterByPriorities(selectedPriorites, false)
        }

        //filtering by date
        else if(filterOptions.children.contains(dateFilter)){
            model.filterByDates(selectedDates, false)
        }

        else if(filterOptions.children.contains(labelFilter)){
            model.filterByLabels(selectedLabels, false)
        }

        //default case, set filtered set as all current items
        else{
            model.filterByPriorities(emptySet<Int>().toMutableSet(), false)
        }
    }
}