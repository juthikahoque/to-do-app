package frontend.views

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color

class LabelView(labelVal: String): HBox() {

    init {
        val label = Label(labelVal).apply {
            id = "tags"
        }
        label.prefWidth = 50.0
        label.alignment = Pos.CENTER
        val backgroundColor = Color.web("#cdb2d4")

        label.background = Background(BackgroundFill(backgroundColor, CornerRadii(5.0), Insets(0.0)))
        children.add(label)
    }
}