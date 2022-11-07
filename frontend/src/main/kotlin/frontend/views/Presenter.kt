package views

import BoardView
import CreateBoardView
import IView
import Model
import SidebarView
import ToolbarView
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
            val createBoardBoarderPane = BorderPane()
            createBoardBoarderPane.background = Background(BackgroundFill(Color.rgb(50, 50, 50, 0.8), CornerRadii(0.9), Insets(0.0)))
            createBoardBoarderPane.center = createBoardView
            applicationStackPane.children.addAll(applicationContainer, createBoardBoarderPane)
        } else {
            applicationStackPane.children.add(applicationContainer)
        }

        children.add(applicationStackPane)
    }

    init {
        HBox.setHgrow(board, Priority.ALWAYS)
        model.addView(this)
    }
}