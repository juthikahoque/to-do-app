package frontend.views

import frontend.Model
import frontend.services.ItemService
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ListChangeListener
import javafx.geometry.Insets
import javafx.scene.control.ChoiceBox
import javafx.scene.control.DateCell
import javafx.scene.control.DatePicker
import javafx.scene.control.ToggleButton
import javafx.scene.control.skin.DatePickerSkin
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.util.StringConverter
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min


//View for the filtering by due date, labels and priorities
class FilterView(private val model: Model) : VBox() {
    private var filter = SimpleObjectProperty(model.noFilter)

    private var iniDay: Int? = null
    private var endDay: Int? = null

    private var iniDate: LocalDate = LocalDate.now()
    private var endDate: LocalDate = LocalDate.now()

    private val datePicker = DatePicker().apply {
        minWidth = 200.0
        isEditable = false
        converter = object : StringConverter<LocalDate?>() {
            override fun toString(obj: LocalDate?): String {
                return if (iniDate != endDate) "$iniDate to $endDate" else obj.toString()
            }

            override fun fromString(str: String): LocalDate? {
                return if (str.contains(" to ")) {
                    iniDate = LocalDate.parse(str.split(" to ").first())
                    endDate = LocalDate.parse(str.split(" to ").last())
                    iniDate
                } else {
                    LocalDate.parse(str)
                }
            }
        }
        value = LocalDate.now()

        setOnShowing {
            val content = (skin as DatePickerSkin).popupContent
            val cells: List<DateCell> = content.lookupAll(".day-cell")
                .filter { "next-month" !in it.styleClass && "previous-month" !in it.styleClass }
                .map { it as DateCell }

            iniDay = null
            endDay = null

            content.setOnMouseDragged { e: MouseEvent ->
                val n = e.pickResult.intersectedNode
                val c = if (n is DateCell) n else if (n is Text) (n.parent as DateCell) else null
                if (c != null && c.styleClass.contains("day-cell") &&
                    "next-month" !in c.styleClass && "previous-month" !in c.styleClass
                ) {
                    iniDay = iniDay ?: c.text.toInt()
                    endDay = c.text.toInt()
                }
                if (iniDay != null && endDay != null) {
                    cells.forEach { it.styleClass.remove("selected") }
                    cells.filter { it.text.toInt() in min(iniDay!!, endDay!!)..max(iniDay!!, endDay!!) }
                        .forEach { it.styleClass.add("selected") }
                }
            }
            content.setOnMouseReleased { e ->
                val ini = iniDay
                val end = endDay
                if (ini != null && end != null) {
                    iniDate = LocalDate.of(
                        value.year,
                        value.month,
                        min(ini, end)
                    )
                    endDate = LocalDate.of(
                        value.year,
                        value.month,
                        max(ini, end)
                    )

                    value = iniDate

                    cells.forEach { it.styleClass.remove("selected") }
                    cells.filter { it.text.toInt() in ini..end }
                        .forEach { it.styleClass.add("selected") }
                } else {
                    val n = e.pickResult.intersectedNode
                    val c = if (n is DateCell) n else if (n is Text) (n.parent as DateCell) else null
                    if (c != null && c.styleClass.contains("day-cell") &&
                        "next-month" !in c.styleClass && "previous-month" !in c.styleClass
                    ) {
                        value = LocalDate.of(
                            value.year,
                            value.month,
                            c.text.toInt()
                        )
                        iniDate = value
                        endDate = value
                    }
                }
                filter.set { bid, sort, order, search ->
                    println("$iniDate to $endDate")
                    ItemService.getItems(
                        bid,
                        startDate = iniDate.atStartOfDay(),
                        endDate = endDate.plusDays(1).atStartOfDay(),
                        search = search,
                        sortBy = sort,
                        orderBy = order
                    )
                }
                endDay = null
                iniDay = null

                if (value == iniDate) {
                    value = value.plusDays(1)
                    value = value.minusDays(1)
                }
                hide()
            }
        }
    }

    private val selectedLabels = mutableSetOf<models.Label>()
    private val labelFilter = HBox().apply {
        spacing = 10.0
    }

    private fun addLabelsToLabelFilter() {
        labelFilter.children.clear()
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
                            search = search,
                            sortBy = sort,
                            orderBy = order
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
                            priorities = selectedPriorities,
                            search = search,
                            sortBy = sort,
                            orderBy = order
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
                "Due Date" -> updateOptions(datePicker)
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

        model.items.addListener(ListChangeListener {
            addLabelsToLabelFilter()
        })

        filter.addListener { _, _, new -> model.filter.set(new) }
    }
}