package frontend.views

import frontend.Model
import javafx.geometry.Insets
import javafx.scene.layout.*
import javafx.scene.paint.Color

class Presenter(private val model: Model) : StackPane() {
    companion object {
        const val addUser = "addUser"
        const val newBoard = "newBoard"
        const val editBoard = "editBoard"
        const val createItem = "createItem"
        const val editItem = "editItem"
        const val helpMenu = "helpMenu"
    }

    private var sidebar = SidebarView(model).apply {
        HBox.setHgrow(this, Priority.ALWAYS)
    }
    private var toolbar = ToolbarView(model)
    private var board = BoardView(model)
    private val boardContainer = VBox(toolbar, board)

    private fun updateView(modal: String) {
        children.clear()

        val applicationContainer = HBox(sidebar, boardContainer)
        val applicationStackPane = StackPane()

        when (modal) {
            editBoard, newBoard -> {
                val itemModal = if (modal == editBoard) {
                    BoardModalView(model, model.currentBoard.value!!)
                } else {
                    BoardModalView(model, null)
                }
                val createBoardBorderPane = BorderPane()
                createBoardBorderPane.background =
                    Background(BackgroundFill(Color.rgb(50, 50, 50, 0.8), CornerRadii(0.9), Insets(0.0)))
                createBoardBorderPane.center = itemModal
                applicationStackPane.children.addAll(applicationContainer, createBoardBorderPane)

                children.add(applicationStackPane)
                itemModal.requestFocus()
            }

            addUser -> {
                val addUserModalView = AddUsersModalView(model)
                val addUserBorderPane = BorderPane()
                addUserBorderPane.background =
                    Background(BackgroundFill(Color.rgb(50, 50, 50, 0.8), CornerRadii(0.9), Insets(0.0)))
                addUserBorderPane.center = addUserModalView
                applicationStackPane.children.addAll(applicationContainer, addUserBorderPane)

                children.add(applicationStackPane)
                addUserModalView.requestFocus()
            }

            createItem, editItem -> {
                val itemModal = if (modal == editItem) {
                    ItemModalView(model, model.currentItem.value!!)
                } else {
                    ItemModalView(model, null)
                }
                val editItemBorderPane = BorderPane()
                editItemBorderPane.background = Background(BackgroundFill(Color.rgb(50, 50, 50, 0.8), CornerRadii(0.9), Insets(0.0)))
                editItemBorderPane.center = itemModal
                applicationStackPane.children.addAll(applicationContainer, editItemBorderPane)

                children.add(applicationStackPane)
                itemModal.requestFocus()
            }

            helpMenu -> {
                val helpModal = HotkeysModalView(model)
                val helpModelBorderPane = BorderPane()
                helpModelBorderPane.background = Background(BackgroundFill(Color.rgb(50, 50, 50, 0.8), CornerRadii(0.9), Insets(0.0)))
                helpModelBorderPane.center = helpModal
                applicationStackPane.children.addAll(applicationContainer, helpModelBorderPane)
                children.add(applicationStackPane)
                helpModal.requestFocus()

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
