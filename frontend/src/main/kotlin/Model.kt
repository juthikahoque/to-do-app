
import models.Board
import models.Item
import models.Label
import services.BoardService
import java.time.LocalDateTime
import java.util.*
import kotlinx.coroutines.runBlocking
import services.ItemService
import utils.ApplicationState

class Model {
    private val views: ArrayList<IView> = ArrayList()
    private var boards: List<Board>
    private var items: List<Item>
    private var currentBoardIdx = 0
    private val userId = UUID.fromString("bf80d583-978e-47df-879e-d1f751aafb46")
    private var applicationState = ApplicationState.Loading

    var showCreateBoard = false

    init {
        runBlocking {
            boards = getBoards()
            // TODO: temporary check if empty; "All" and "Personal" boards should be created
            //         by default when a user creates an account
            if (boards.isEmpty()) {
                BoardService.addBoard(Board("All", mutableSetOf(userId)))
                BoardService.addBoard(Board("Personal", mutableSetOf(userId)))
            }
            items = getItems(boards[0].id)
            applicationState = ApplicationState.Ready
        }
    }

    fun addView(view: IView) {
        views.add(view)
        view.updateView()
    }

    private fun notifyObservers() {
        for (view in views) {
            view.updateView()
        }
    }

    fun getApplicationState(): ApplicationState {
        return applicationState
    }

    fun updateCurrentBoard(idx: Int) {
        if (idx !== currentBoardIdx) {
            currentBoardIdx = idx
            applicationState = ApplicationState.Loading
            runBlocking {
                items = getItems(boards[currentBoardIdx].id)
            }
            applicationState = ApplicationState.Ready
        }
    }

    fun getCurrentBoard(): Board {
        return boards[currentBoardIdx]
    }

    fun addBoard(board: Board){
        runBlocking {
            BoardService.addBoard(board)
        }
        notifyObservers()
    }

    fun getBoards(): List<Board>{
        lateinit var boards: List<Board>
        runBlocking {
            boards = BoardService.getBoards()
        }
        return boards
    }

    fun getItems(boardId: UUID): List<Item> {
        lateinit var items: List<Item>
        runBlocking {
            items = ItemService.getItems(boardId)
        }
        return items
    }

    fun addToDoItem(item: Item) {
        // items.add(item)
        notifyObservers()
    }

    fun setCreateBoardMenu(toOpen:Boolean){
        showCreateBoard = toOpen
        notifyObservers()
    }

    fun logout(){
        println("Logged out!")
    }
}
