package frontend

import frontend.services.AuthService
import frontend.services.BoardService
import frontend.services.ItemService
import frontend.utils.Actions
import frontend.utils.ApplicationState
import frontend.utils.UndoRedoManager
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
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
                    "Personal",
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

        currentBoard.addListener { _, _, _ -> launch { updateItems() } }
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

    fun addBoard(board: Board) {
        UndoRedoManager.handleAction(Actions.addBoard, items, boards, null)
        runBlocking {
            BoardService.addBoard(board)
            updateBoards()
        }
    }

    fun addToDoItem(item: Item) {
        UndoRedoManager.handleAction(Actions.addItem, items, boards, null)
        runBlocking {
            ItemService.addItem(item.boardId, item)
            updateItems()
        }
    }
        

    fun customOrderEnabled(): Boolean {
        return (currentBoard.value != allBoard
                && filter.value == noFilter
                && sort.value == ""
                && search.value == "")
    }

    fun updateItem(item: Item) {
        UndoRedoManager.handleAction(Actions.updateItem, items, boards, null)
        runBlocking {
            ItemService.updateItem(item.boardId, item)
            updateItems()
        }
    }

    fun updateBoard(board: Board) {
        UndoRedoManager.handleAction(Actions.updateBoard, items, boards, null)
        runBlocking {
            BoardService.updateBoard(board)
            updateBoards()
        }
    }

    fun toggleMode(mode: String) {
        app.changeThemeMode(mode, "main")
    }
}
