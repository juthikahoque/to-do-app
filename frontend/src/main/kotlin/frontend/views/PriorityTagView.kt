package frontend.views

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.HBox
import javafx.scene.paint.Color

class PriorityTagView(priorityLevel: Int, showColor: Boolean = true) : HBox() {
    fun getColor(priorityLevel: Int): Color {
        return when (priorityLevel) {
            0 -> Color.web("#2af7a5")
            1 -> Color.web("#f7942a")
            2 -> Color.web("#f72a50")
            else -> Color.web("#FFFFFF")
        }
    }

    init {
        val label = Label().apply {
            text = when (priorityLevel) {
                0 -> "Low"
                1 -> "Medium"
                2 -> "High"
                else -> ""
            }

            prefWidth = 50.0
            alignment = Pos.CENTER

            val backgroundColor = if (showColor) {
                getColor(priorityLevel)
            } else {
                getColor(-1)
            }
            background = Background(BackgroundFill(backgroundColor, CornerRadii(5.0), Insets(0.0)))
        }

        children.add(label)
    }
}