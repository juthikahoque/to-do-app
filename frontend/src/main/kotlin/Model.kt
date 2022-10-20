import javafx.application.Platform
import models.Board
import services.BoardService
import kotlinx.coroutines.runBlocking

class Model {
    private val views: ArrayList<IView> = ArrayList()
    private val boards: ArrayList<Board> = ArrayList()
    init {
        runBlocking {
            BoardService.addBoard(Board("All"))
            BoardService.addBoard(Board("Personal"))
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