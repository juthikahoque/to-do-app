package frontend

import frontend.interfaces.IView
import frontend.services.AuthService
import frontend.services.BoardService
import frontend.services.ItemService
import frontend.utils.ApplicationState
import kotlinx.coroutines.runBlocking
import models.Board
import models.Item
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class Model {
    private val searchFilterSort: ArrayList<IView> = ArrayList()
    private val views: ArrayList<IView> = ArrayList()
    private var boards: List<Board>
    private var currentBoardIdx = 0
    private var applicationState = ApplicationState.Loading

    private var currItems: MutableList<Item> //current list of items to display

    // We keep track of the result of the current filter, search and sort settings
    // on all the items in individual sets. This allows us to display and add notes with
    // all the settings applied at the same time.

    private var currFilter:Set<Item> //result of current filter setting
    private var currSort:Set<Item> //result of current sort setting
    private var currSearch:Set<Item> //result of current search string

    var showCreateBoard = false

    init {
        runBlocking {
            boards = getBoards()
        }

        // TODO: temporary check if empty; "All" and "Personal" boards should be created
        //         by default when a user creates an account
        if (boards.isEmpty()) {
            runBlocking {
                BoardService.addBoard(Board("All", mutableSetOf(AuthService.user!!.localId)))
                BoardService.addBoard(Board("Personal", mutableSetOf(AuthService.user!!.localId)))
                boards = getBoards()
            }
        }

        currItems = getItems(getCurrentBoard().id).toMutableList()
        currFilter = currItems.toMutableSet()
        currSearch = currItems.toMutableSet()
        currSort = currItems.toMutableSet()
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

    // Since sort/filter/search mutate the notes to be displayed,
    // we may need to notify them before notifying the other observers
    fun addSearchFilterSort(view:IView){
        searchFilterSort.add(view)
    }

    private fun notifySearchFilterSort(){
        for(view in searchFilterSort){
            view.updateView()
        }
    }

    fun getApplicationState(): ApplicationState {
        return applicationState
    }

    fun getCurrentBoard(): Board {
        return boards[currentBoardIdx]
    }

    fun getUsername(): String? {
        return AuthService.user?.displayName
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
        lateinit var allItems: List<Item>
        runBlocking {
            allItems = ItemService.getItems(boardId).toMutableList()
        }
        return allItems
    }

    fun getCurrentItems():List<Item>{
        return currItems
    }

    fun addToDoItem(item: Item) {
        runBlocking {
            ItemService.addItem(item.boardId, item)
        }
        //add item, notify mutators, apply the settings and then notify displaying views
        currItems = getItems(getCurrentBoard().id).toMutableList()
        notifySearchFilterSort()
        applySearchFilterSort()
        notifyObservers()
    }

    fun setCreateBoardMenu(toOpen:Boolean) {
        showCreateBoard = toOpen
        notifyObservers()
    }

    //apply all three of search, filter and sort at the same time
    //by taking the intersection of their individual sets
    private fun applySearchFilterSort(){
        val filterAndSort= currSort.intersect(currFilter)
        val searchSortAndFilter = filterAndSort.intersect(currSearch)
        currItems = searchSortAndFilter.toMutableList()
    }

    fun changeItemOrder(from:Int, to:Int){
        runBlocking {
            currSort = ItemService.orderItem(boards[currentBoardIdx].id, from, to)
        }
        applySearchFilterSort()
        notifyObservers()
    }

    fun filterByPriorities(priorities: MutableSet<Int>, notify:Boolean = true) {
        runBlocking {
            currFilter = ItemService.filterByPriorities(boards[currentBoardIdx].id, priorities)
        }

        //notify all observer only if we're not notifying just the mutators
        if(notify) {
            applySearchFilterSort()
            notifyObservers()
        }
    }

    fun filterByDates(dates:Pair<LocalDate,LocalDate?>, notify:Boolean = true) {
        runBlocking {
            currFilter = ItemService.filterByDates(boards[currentBoardIdx].id, dates.first.atStartOfDay(), dates.second?.atStartOfDay())
        }

        //notify all observer only if we're not notifying just the mutators
        if(notify) {
            applySearchFilterSort()
            notifyObservers()
        }
    }

    fun sortItems(sortBy:String, orderBy:String, notify:Boolean = true){
        runBlocking {
            currSort = ItemService.sort(getCurrentBoard().id, sortBy, orderBy)
        }

        //notify all observer only if we're not notifying just the mutators
        if(notify) {
            applySearchFilterSort()
            notifyObservers()
        }
    }

    fun searchItems(searchString:String, notify:Boolean = true){
        runBlocking {
            currSearch = ItemService.search(getCurrentBoard().id, searchString)
        }

        //notify all observer only if we're not notifying just the mutators
        if(notify) {
           applySearchFilterSort()
           notifyObservers()
       }
    }

    fun logout(){
        AuthService.logout()
        app.changeScene("login")
    }
}
