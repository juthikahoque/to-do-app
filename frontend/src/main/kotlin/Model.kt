import javafx.application.Platform

class Model {
    private val views: ArrayList<IView> = ArrayList()
    private val boards: ArrayList<String> = ArrayList()
    init {
        boards.add("All")
        boards.add("Personal")
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

    fun addBoard(boardName:String){
        boards.add(boardName)
        notifyObservers()
    }

    fun getBoards(): ArrayList<String>{
        return boards
    }

    fun logout(){
        Platform.exit()
    }
}