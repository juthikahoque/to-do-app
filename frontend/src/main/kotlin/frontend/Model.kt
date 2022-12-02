package frontend

import frontend.services.AuthService
import frontend.services.BoardService
import frontend.services.ItemService
import frontend.utils.ApplicationState
import frontend.utils.UndoRedoManager
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import models.Board
import models.Item

class Model : CoroutineScope {
    override val coroutineContext = Dispatchers.JavaFx

    val allBoard = Board("All")

    val boards: ObservableList<Board> = FXCollections.observableArrayList()
    val currentBoard = SimpleObjectProperty<Board>()
    val items: ObservableList<Item> = FXCollections.observableArrayList()
    val currentItem = SimpleObjectProperty<Item?>()

    val noFilter: suspend (String, String, String, String) -> List<Item> =
        { bid, sort, order, search -> ItemService.getItems(bid, sortBy = sort, orderBy = order, search = search) }

    val sort = SimpleStringProperty("")
    val order = SimpleStringProperty("")
    val search = SimpleStringProperty("")
    val filter = SimpleObjectProperty(noFilter) // function that takes bid, sort, order, search

    val applicationState = SimpleObjectProperty(ApplicationState.Loading)

    val additionalModalView = SimpleStringProperty("") // Presenter companion Enum

    init {
        runBlocking {
            val list = BoardService.getBoards().toMutableList()
            if (list.isEmpty()) { // user has no board, add a personal
                val personalBoard = Board(
                    "My Board",
                    mutableSetOf(AuthService.user)
                )
                BoardService.addBoard(personalBoard)
                list.add(personalBoard)
            }
            list.add(0, allBoard)
            boards.setAll(list)
        }

        UndoRedoManager.init(this)

        sort.addListener { _, _, _ -> launch { updateItems() } }
        order.addListener { _, _, _ -> launch { updateItems() } }
        search.addListener { _, _, _ -> launch { updateItems() } }
        filter.addListener { _, _, _ -> launch { updateItems() } }

        currentBoard.addListener { _, old, new -> if (new?.id != old?.id) launch { updateItems() } }
        currentBoard.set(boards[1])

        applicationState.set(ApplicationState.Ready)
    }

    suspend fun updateBoards() {
        val list = BoardService.getBoards().toMutableList()
        list.add(0, allBoard)
        boards.setAll(list)
    }

    suspend fun updateItems() {
        val boardIdStr = if (currentBoard.value == allBoard) "all" else currentBoard.value.id.toString()
        val sortStr = if (sort.value == "" && currentBoard.value == allBoard) "dueDate" else sort.value
        val orderStr = order.value
        val searchStr = search.value

        items.setAll(
            filter.value.invoke(
                boardIdStr,
                sortStr,
                orderStr,
                searchStr,
            )
        )
    }

    fun customOrderEnabled(): Boolean {
        return (currentBoard.value != allBoard
                && filter.value == noFilter
                && sort.value == ""
                && search.value == "")
    }
}
