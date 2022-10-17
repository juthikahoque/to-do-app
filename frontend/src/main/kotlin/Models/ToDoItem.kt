package Models

import java.util.*

class ToDoItem(
    var title: String,
    var date: Date,
    var priority: ToDoPriority?,
    var label: String?,
    var user: String
) {
    var completed: Boolean = false

    fun toggleCompleted() {
        completed = !completed
    }
}