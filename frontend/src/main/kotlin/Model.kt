import javafx.application.Platform
import models.Board

class Model {
    private val views: ArrayList<IView> = ArrayList()
    private val boards: ArrayList<Board> = ArrayList()
    init {
        boards.add(Board("All"))
        boards.add(Board("Personal"))
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
        boards.add(board)
        notifyObservers()
    }

    fun getBoards(): ArrayList<Board>{
        return boards
    }

    fun logout(){
        println("Logged out!")
    }
}