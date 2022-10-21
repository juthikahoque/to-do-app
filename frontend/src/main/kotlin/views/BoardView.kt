import javafx.geometry.*
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.text.Font

class BoardView(private val model: Model): VBox(), IView {

    private var createToDoHeader = Label("Create To-Do item:")
    private var createToDoRowView = CreateToDoRowView(model)
    private var myToDosHeader = Label("My To-Dos:")

    override fun updateView(){
        children.clear()

        createToDoHeader.padding = Insets(0.0, 0.0, 10.0, 0.0)
        createToDoHeader.font = Font("Regular", 16.0)
        children.add(createToDoHeader)

        children.add(createToDoRowView)

        myToDosHeader.padding = Insets(20.0, 0.0, 10.0, 0.0)
        myToDosHeader.font = Font("Regular", 16.0)
        children.add(myToDosHeader)
        for (item in model.getItems()) {
            children.add(ToDoRowView(item))
        }
    }

    init {
        padding = Insets(10.0)
        model.addView(this)
    }
}