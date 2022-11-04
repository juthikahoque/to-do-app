
import models.*
import services.*
import java.util.*
import kotlinx.coroutines.runBlocking
import services.ItemService
import utils.ApplicationState

class Model {
    private val views: ArrayList<IView> = ArrayList()
    private var boards: List<Board>
    private var currentBoardIdx = 0
    private var applicationState = ApplicationState.Loading

    var showCreateBoard = false

    init {
        runBlocking {
            boards = getBoards()
        }

        // TODO: temporary check if empty; "All" and "Personal" boards should be created
        //         by default when a user creates an account
        if (boards.isEmpty()) {
            runBlocking {
                BoardService.addBoard(Board("All", mutableSetOf(AuthService.user.localId)))
                BoardService.addBoard(Board("Personal", mutableSetOf(AuthService.user.localId)))
                boards = getBoards()
            }
        }
        applicationState = ApplicationState.Ready
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

    fun getCurrentBoard(): Board {
        return boards[currentBoardIdx]
    }

    fun updateCurrentBoard(idx: Int) {
        if (idx != currentBoardIdx) {
            currentBoardIdx = idx
            applicationState = ApplicationState.Loading
            getItems(boards[currentBoardIdx].id)
            applicationState = ApplicationState.Ready
            notifyObservers()
        }
    }

    fun getBoards(): List<Board> {
        runBlocking {
            boards = BoardService.getBoards()
        }
        boards = boards
        return boards
    }

    fun addBoard(board: Board){
        runBlocking {
            BoardService.addBoard(board)
        }
        showCreateBoard = false
        notifyObservers()
    }

    fun getItems(boardId: UUID): List<Item> {
        lateinit var items: List<Item>
        runBlocking {
            items = ItemService.getItems(boardId)
        }
        return items
    }

    fun addToDoItem(item: Item) {
        runBlocking {
            ItemService.addItem(item.boardId, item)
        }
        notifyObservers()
    }

    fun setCreateBoardMenu(toOpen:Boolean) {
        showCreateBoard = toOpen
        notifyObservers()
    }

    fun logout(){
        println("Logged out!")
    }
}
