package frontend.views

import frontend.Model
import frontend.services.ItemService
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.scene.control.ChoiceBox
import javafx.scene.control.DatePicker
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.layout.*
import javafx.scene.paint.Color
import java.time.LocalDate

//View for the filtering by due date, labels and priorities
class FilterView(private val model: Model) : VBox() {

    private var oldestDate = LocalDate.MIN

    private var selectedDates: Pair<LocalDate, LocalDate?> = Pair(oldestDate, null)

    private var filter = SimpleObjectProperty(model.noFilter)

    private val dateFilter = HBox().apply {
        val from = DatePicker().apply {
            prefWidth = 125.0
            setOnAction {
                selectedDates = selectedDates.copy(first = value)
                filter.set { bid, sort, order, search ->
                    ItemService.getItems(
                        bid,
                        startDate = selectedDates.first.atStartOfDay(),
                        endDate = selectedDates.second?.atStartOfDay(),
                        search =  search,
                        sortBy =  sort,
                        orderBy =  order
                    )
                }
            }

        }
        val toLabel = Label(" to ").apply { translateY = 5.0 }
        val to = DatePicker().apply {
            prefWidth = 125.0
            setOnAction {
                selectedDates = selectedDates.copy(second = value)
                filter.set { bid, sort, order, search ->
                    ItemService.getItems(
                        bid,
                        startDate = selectedDates.first.atStartOfDay(),
                        endDate = selectedDates.second?.atStartOfDay(),
                        search =  search,
                        sortBy =  sort,
                        orderBy =  order
                    )
                }
            }
        }
        children.addAll(from, toLabel, to)
    }

    private val selectedLabels = mutableSetOf<models.Label>()
    private val labelFilter = HBox().apply {
        spacing = 10.0
    }
    private fun addLabelsToLabelFilter() {
        labelFilter.children.setAll(model.currentBoard.value.labels.map { label ->
            ToggleButton(label.value).apply {
                isSelected = selectedLabels.contains(label)
                id = "labelToggle"
                background = if (isSelected) {
                    Background(BackgroundFill(Color.LIGHTBLUE, CornerRadii(5.0), Insets(0.0)))
                } else {
                    Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
                }
                setOnAction {
                    if (isSelected) {
                        background = Background(BackgroundFill(Color.LIGHTBLUE, CornerRadii(5.0), Insets(0.0)))
                        selectedLabels.add(label)
                    } else {
                        background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
                        if (selectedLabels.contains(label)) {
                            selectedLabels.remove(label)
                        }
                    }
                    filter.set { bid, sort, order, search ->
                        ItemService.getItems(
                            bid,
                            labels = selectedLabels,
                            search =  search,
                            sortBy =  sort,
                            orderBy =  order
                        )
                    }
                }
            }
        })
    }

    private val selectedPriorities = mutableSetOf<Int>()
    private val priorityGroup = HBox().apply {
        val priorityView = PriorityTagView(0, true)
        val labels = listOf("Low", "Medium", "High")
        for ((index, label) in labels.withIndex()) {
            val toggle = ToggleButton(label).apply {
                id = label.lowercase()
                background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
                val backgroundColor = priorityView.getColor(index)
                setOnAction {
                    if (isSelected) {
                        background = Background(BackgroundFill(backgroundColor, CornerRadii(5.0), Insets(0.0)))
                        selectedPriorities.add(index)
                    } else {
                        background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
                        if (selectedPriorities.contains(index)) {
                            selectedPriorities.remove(index)
                        }
                    }
                    filter.set { bid, sort, order, search ->
                        ItemService.getItems(
                            bid,
                            priorities =  selectedPriorities,
                            search =  search,
                            sortBy =  sort,
                            orderBy =  order
                        )
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
            when (value) {
                "Due Date" -> updateOptions(dateFilter)
                "Priority" -> updateOptions(priorityGroup)
                "Labels" -> updateOptions(labelFilter)
            }
        }
    }

    private val filterOptions = HBox(filterChoice).apply {
        spacing = 5.0
    }

    private fun updateOptions(newChild: Region?) {
        if (filterOptions.children.size > 1) {
            filterOptions.children.removeAt(1)
        }
        filterOptions.children.add(newChild)
    }

    private fun showOptions(show: Boolean) {
        if (show) children.add(filterOptions) else children.remove(filterOptions)
    }

    private val filterButton = ToggleButton("Filter").apply {
        id = "general"
        background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii(5.0), Insets(0.0)))
        prefWidth = 125.0
        setOnAction {
            showOptions(isSelected)
            if (isSelected) {
                background = Background(BackgroundFill(Color.DARKGRAY, CornerRadii(5.0), Insets(0.0)))
                //re-apply the old filters
                model.filter.set(filter.value)
            } else {
                background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii(5.0), Insets(0.0)))
                //switch off all filters
                model.filter.set(model.noFilter)
            }
        }
    }

    init {
        children.addAll(filterButton)
        spacing = 5.0

        model.currentBoard.addListener { _, _, _ ->
            addLabelsToLabelFilter()
        }

        filter.addListener { _, _, new -> model.filter.set(new) }
    }
}