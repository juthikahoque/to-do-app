import javafx.scene.control.Toggle
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup


/*
    Controller for the sidebar, implements these functions:
    - Adding new boards
    - Logging out
    - Controlling the boards toggle group such that exactly one board is always selected
    -
*/
class SidebarController(private val model: Model) {

    fun addNewBoard(){
        model.addBoard("New board added")
    }

    fun logout(){
        model.logout()
    }

    fun setToggleGroupProperties(group:ToggleGroup){
        group.apply {
            selectedToggleProperty().addListener { _, oldValue, newValue ->
                if (newValue == null) oldValue.isSelected = true
            }
        }
    }

    //TODO: Add styles in CSS sheet with additional hover properties
    fun setBoardButtonProperties(button:ToggleButton){
        button.apply {
            selectedProperty().addListener { _, _, newValue ->
                if(isSelected){
                    style = "-fx-background: rgb(169, 169, 169);\n -fx-background-color: rgb(169, 169, 169)"                    //TODO: update views to show items in the selected board
                }
                else {
                    style = "-fx-background: rgb(52, 52, 54);\n -fx-background-color: rgb(52, 52, 54)"
                }
            }
        }
    }
}