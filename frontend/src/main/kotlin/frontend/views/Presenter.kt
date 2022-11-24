package frontend.views

import frontend.Model
import javafx.geometry.Insets
import javafx.scene.layout.*
import javafx.scene.paint.Color

class Presenter(private val model: Model): StackPane() {
    companion object {
        const val newBoard = "newBoard"
        const val addUser = "addUser"
        const val editItem = "editItem"
    }

    private var sidebar = SidebarView(model)
    private var toolbar = ToolbarView(model)
    private var board = BoardView(model)
    private val boardContainer = VBox(toolbar, board)

    private fun updateView(modal: String) {
        children.clear()

        val applicationContainer = HBox(sidebar, boardContainer)
        val applicationStackPane = StackPane()

        when (modal) {
            newBoard -> {
                val createBoardView = CreateBoardView(model)
                val createBoardBorderPane = BorderPane()
                createBoardBorderPane.background = Background(BackgroundFill(Color.rgb(50, 50, 50, 0.8), CornerRadii(0.9), Insets(0.0)))
                createBoardBorderPane.center = createBoardView
                applicationStackPane.children.addAll(applicationContainer, createBoardBorderPane)

                children.add(applicationStackPane)
                createBoardView.requestFocus()
            }
            addUser -> {
                val addUserModalView = AddUsersModalView(model)
                val addUserBorderPane = BorderPane()
                addUserBorderPane.background = Background(BackgroundFill(Color.rgb(50, 50, 50, 0.8), CornerRadii(0.9), Insets(0.0)))
                addUserBorderPane.center = addUserModalView
                applicationStackPane.children.addAll(applicationContainer, addUserBorderPane)

                children.add(applicationStackPane)
                addUserModalView.requestFocus()
            }
            editItem -> {
                val editItemModal = EditItemModalView(model, model.currentItem.value!!)
                val editItemBorderPane = BorderPane()
                editItemBorderPane.background = Background(BackgroundFill(Color.rgb(50, 50, 50, 0.8), CornerRadii(0.9), Insets(0.0)))
                editItemBorderPane.center = editItemModal
                applicationStackPane.children.addAll(applicationContainer, editItemBorderPane)

                children.add(applicationStackPane)
                editItemModal.requestFocus()
            }
            else -> {
                applicationStackPane.children.add(applicationContainer)

                children.add(applicationStackPane)
            }
        }
    }

    init {
        HBox.setHgrow(board, Priority.ALWAYS)
        HBox.setHgrow(boardContainer, Priority.ALWAYS)
        //HBox.setHgrow(this, Priority.ALWAYS)

        model.additionalModalView.addListener { _, _, new -> updateView(new) }
        updateView("")
    }
}
