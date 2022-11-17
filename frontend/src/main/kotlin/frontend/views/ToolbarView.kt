package frontend.views

import frontend.Model
import frontend.interfaces.IView
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.*

//View for the toolbar, includes a search bar
//invite button and options to filter/sort

class ToolbarView(private val model: Model): BorderPane(), IView {
    private val searchField = TextField().apply {
        promptText = "Search"
        minWidth = 400.0
        HBox.setHgrow(this, Priority.NEVER)
        textProperty().addListener { _, _, newValue ->
            model.searchItems(newValue)
        }
    }

    private val inviteGroup:VBox = VBox().apply {
        padding = Insets(0.0, 10.0, 10.0, 10.0)
        spacing = 10.0
        HBox.setHgrow(this, Priority.ALWAYS)
    }

    private var inviteButton = Button("Invite").apply {
        minWidth = 75.0
        setOnAction {
            if(inviteGroup.children.size == 1) {
                inviteGroup.children.remove(this)
                inviteGroup.children.addAll(inviteField,  this)
            }
            else{
                inviteGroup.children.removeAt(0)
            }

        }
    }

    private val inviteField  = TextField().apply {
        promptText = "Username"
    }

    private val sort = SortView(model)

    private val filter = FilterView(model)

    private val sortFilterGroup = HBox().apply {
        padding = Insets(10.0, 0.0, 0.0, 0.0)
        spacing = 15.0
        children.addAll(sort, filter)
    }


    override fun updateView() {
        model.searchItems(searchField.text, false)
    }

    init {
        inviteGroup.children.add(inviteButton)
        left = VBox(searchField, sortFilterGroup).apply {
            HBox.setHgrow(this, Priority.NEVER)
        }

        //add a pane that always grow to make the UI responsive
        val spacer = Pane().apply{
            HBox.setHgrow(this, Priority.ALWAYS)
        }
        center = spacer
        right = inviteGroup
        padding = Insets(10.0)
        HBox.setHgrow(this, Priority.ALWAYS)

        model.addSearchFilterSort(filter)
        model.addSearchFilterSort(sort)
        model.addSearchFilterSort(this)
    }
}