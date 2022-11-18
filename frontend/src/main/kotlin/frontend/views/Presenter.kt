package frontend.views

import frontend.Model
import frontend.interfaces.IView
import javafx.geometry.Insets
import javafx.scene.layout.*
import javafx.scene.paint.Color

class Presenter(private val model: Model): StackPane(), IView {
    private var sidebar = SidebarView(model)
    private var toolbar = ToolbarView(model)
    private var board = BoardView(model)
    private val boardContainer = VBox(toolbar, board)

    override fun updateView() {
        children.clear()

        val applicationContainer = HBox(sidebar, boardContainer)
        val applicationStackPane = StackPane()

        if (model.showCreateBoard) {
            val createBoardView = CreateBoardView(model)
            val createBoardBorderPane = BorderPane()
            createBoardBorderPane.background = Background(BackgroundFill(Color.rgb(50, 50, 50, 0.8), CornerRadii(0.9), Insets(0.0)))
            createBoardBorderPane.center = createBoardView
            applicationStackPane.children.addAll(applicationContainer, createBoardBorderPane)

            children.add(applicationStackPane)
            createBoardView.requestFocus()
        } else if (model.showAddUsersModalView) {
            val addUserModalView = AddUsersModalView(model)
            val addUserBorderPane = BorderPane()
            addUserBorderPane.background = Background(BackgroundFill(Color.rgb(50, 50, 50, 0.8), CornerRadii(0.9), Insets(0.0)))
            addUserBorderPane.center = addUserModalView
            applicationStackPane.children.addAll(applicationContainer, addUserBorderPane)

            children.add(applicationStackPane)
            addUserModalView.requestFocus()
        } else if (model.showEditItemModal) {
            val editItemModal = EditItemModalView(model, model.itemToEdit!!)
            val editItemBorderPane = BorderPane()
            editItemBorderPane.background = Background(BackgroundFill(Color.rgb(50, 50, 50, 0.8), CornerRadii(0.9), Insets(0.0)))
            editItemBorderPane.center = editItemModal
            applicationStackPane.children.addAll(applicationContainer, editItemBorderPane)

            children.add(applicationStackPane)
            editItemModal.requestFocus()
        } else {
            applicationStackPane.children.add(applicationContainer)

            children.add(applicationStackPane)
        }
    }

    init {
        HBox.setHgrow(board, Priority.ALWAYS)
        HBox.setHgrow(boardContainer, Priority.ALWAYS)
        //HBox.setHgrow(this, Priority.ALWAYS)
        model.addView(this)
    }
}
