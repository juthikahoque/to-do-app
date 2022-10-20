import kotlinx.coroutines.runBlocking
import models.Board
import services.BoardService
import java.util.*

class Model {
    private val views: ArrayList<IView> = ArrayList()
    private val userId = UUID.fromString("bf80d583-978e-47df-879e-d1f751aafb46")
    init {
        runBlocking {
            val boards = BoardService.getBoards()
            if (boards.isEmpty()) {
                BoardService.addBoard(Board("All", mutableSetOf(userId)))
                BoardService.addBoard(Board("Personal", mutableSetOf(userId)))
            }
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

    fun logout(){
        println("Logged out!")
    }
}