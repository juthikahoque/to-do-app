package Models

class Board(var name: String, var creator: User) {
    var todos: MutableList<ToDoItem> = mutableListOf<ToDoItem>()
    var labels: MutableList<String> = mutableListOf<String>()
    var users: MutableList<User> = mutableListOf<User>(creator)
}