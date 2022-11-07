package frontend.views
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color

class PriorityTagView(priorityLevel: Int): HBox() {
    fun getColor(priorityLevel: Int): Color {
        return when (priorityLevel) {
            0 -> Color.web("#00FF00")
            1 -> Color.web("#FFA500")
            2 -> Color.web("#FF0000")
            else -> Color.web("#FFFFFF")
        }
    }

    init {
        val label = Label()
        label.text = when (priorityLevel) {
            0 -> "Low"
            1 -> "Medium"
            2 -> "High"
            else -> ""
        }

        label.prefWidth = 50.0
        label.alignment = Pos.CENTER

        val backgroundColor = getColor(priorityLevel)

        label.background = Background(BackgroundFill(backgroundColor, CornerRadii(5.0), Insets(0.0)))
        children.add(label)
    }
}