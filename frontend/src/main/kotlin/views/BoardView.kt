import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.*

class BoardView(private val model: Model): HBox(), IView {

    private var createToDoRowView = CreateToDoRowView(model)

    override fun updateView(){
        children.clear()
        children.add(createToDoRowView)
    }

    init {
        padding = Insets(10.0)
        model.addView(this)
    }
}