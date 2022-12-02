package frontend.views

import frontend.Model
import frontend.app
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.*

//View for the toolbar, includes a search bar
//invite button and options to filter/sort

class ToolbarView(private val model: Model) : BorderPane() {
    private val searchField = TextField().apply {
        id = "input"
        promptText = "Search"
        minWidth = 400.0
        HBox.setHgrow(this, Priority.NEVER)
    }

    private val createButton = Button().apply {
        id = "create-button"
        prefWidth = 40.0
        prefHeight = 40.0
        setOnAction {
            model.additionalModalView.set(Presenter.createItem)
        }
        model.currentBoard.addListener { _, _, newValue -> isVisible = (newValue != model.allBoard) }

    }


    private var inviteButton = Button().apply {
        id = "invite-button"
        prefWidth = 40.0
        prefHeight = 40.0
        setOnAction {
            model.additionalModalView.set(Presenter.addUser)
        }
        model.currentBoard.addListener { _, _, newValue -> isVisible = (newValue != model.allBoard) }
    }

    private val sort = SortView(model)

    private val filter = FilterView(model)

    private val sortFilterGroup = HBox().apply {
        padding = Insets(10.0, 0.0, 0.0, 0.0)
        spacing = 15.0
        children.addAll(sort, filter)
    }

    private val buttons = HBox(createButton, inviteButton)

    init {
        model.search.bind(searchField.textProperty())

        left = VBox(searchField, sortFilterGroup).apply {
            HBox.setHgrow(this, Priority.NEVER)
        }
        id = "borderPane"

        //add a pane that always grow to make the UI responsive
        val spacer = Pane().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        }
        center = spacer
        padding = Insets(10.0)
        HBox.setHgrow(this, Priority.ALWAYS)

        right = buttons

        app.addHotkey(KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN)) {
            searchField.requestFocus()
        }

        app.addHotkey(KeyCodeCombination(KeyCode.U, KeyCombination.SHORTCUT_DOWN)) {
            if (model.currentBoard.value != model.allBoard && model.additionalModalView.value.isEmpty()) {
                model.additionalModalView.set(Presenter.addUser)
            }
        }
    }
}
