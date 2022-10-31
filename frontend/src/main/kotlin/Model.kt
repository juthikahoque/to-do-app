
import models.*
import services.*
import java.time.LocalDateTime
import java.util.*
import kotlinx.coroutines.runBlocking

class Model {
    private val views: ArrayList<IView> = ArrayList()
    private val boards: ArrayList<Board> = ArrayList()
    private val items: ArrayList<Item> = ArrayList()
    private val currentBoardIdx = 0
    var showCreateBoard = false
    init {
        runBlocking {
            val boards = BoardService.getBoards()
            if (boards.isEmpty()) {
                BoardService.addBoard(Board("All", mutableSetOf(AuthService.user.localId)))
                BoardService.addBoard(Board("Personal", mutableSetOf(AuthService.user.localId)))
            }

            items.add(
                Item(
                    "Do groceries",
                    LocalDateTime.now(),
                    UUID.fromString("bf80d583-978e-47df-879e-d1f751aafb46"),
                    mutableSetOf<Label>(),
                    1,
                    UUID.fromString("bf80d583-978e-47df-879e-d1f751aafb46"),
                    false
                )
            )
            items.add(
                Item(
                    "Clean room",
                    LocalDateTime.now(),
                    UUID.fromString("bf80d583-978e-47df-879e-d1f751aafb46"),
                    mutableSetOf<Label>(),
                    1,
                    UUID.fromString("bf80d583-978e-47df-879e-d1f751aafb46"),
                    false
                )
            )
            items.add(
                Item(
                    "Pick up package",
                    LocalDateTime.now(),
                    UUID.fromString("bf80d583-978e-47df-879e-d1f751aafb46"),
                    mutableSetOf<Label>(),
                    2,
                    UUID.fromString("bf80d583-978e-47df-879e-d1f751aafb46"),
                    false
                )
            )
            items.add(
                Item(
                    "Do laundry",
                    LocalDateTime.now(),
                    UUID.fromString("bf80d583-978e-47df-879e-d1f751aafb46"),
                    mutableSetOf<Label>(),
                    0,
                    UUID.fromString("bf80d583-978e-47df-879e-d1f751aafb46"),
                    false
                )
            )
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

    fun getItems(): List<Item> {
        return items
    }

    fun addToDoItem(item: Item) {
        items.add(item)
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
