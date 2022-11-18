package frontend

import frontend.interfaces.IView
import frontend.services.AuthService
import frontend.services.BoardService
import frontend.services.ItemService
import frontend.utils.ApplicationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.runBlocking
import models.Board
import models.Item
import models.Label
import java.time.LocalDate
import java.util.*

class Model : CoroutineScope {
    private val searchFilterSort: ArrayList<IView> = ArrayList()
    private val views: ArrayList<IView> = ArrayList()
    private var boards: List<Board>
    private var currentBoardIdx = 0
    private var applicationState = ApplicationState.Loading

    private var currItems: MutableList<Item> //current list of items to display

    // We keep track of the result of the current filter, search and sort settings
    // on all the items in individual sets. This allows us to display and add notes with
    // all the settings applied at the same time.

    private var currFilter: List<Item> //result of current filter setting
    private var currSort: List<Item> //result of current sort setting
    private var currSearch: List<Item> //result of current search string

    var showCreateBoard = false
    var showAddUsersModalView = false

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
        currFilter = currItems
        currSearch = currItems
        currSort = currItems
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
    fun addSearchFilterSort(view: IView) {
        searchFilterSort.add(view)
    }

    private fun notifySearchFilterSort() {
        for (view in searchFilterSort) {
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

    fun updateBoard( newBoard: Board){
        runBlocking {
            BoardService.updateBoard(newBoard)
        }

    }

    fun updateCurrentBoard(idx: Int) {
        if (idx != currentBoardIdx) {
            currentBoardIdx = idx
            applicationState = ApplicationState.Loading
            currItems = getItems(boards[currentBoardIdx].id).toMutableList()
            applicationState = ApplicationState.Ready
            notifySearchFilterSort()
            notifyObservers()
        }
    }

    fun getBoards(): List<Board> {
        runBlocking {
            boards = BoardService.getBoards()
        }
        return boards
    }

    fun addBoard(board: Board) {
        runBlocking {
            BoardService.addBoard(board)
        }
        showCreateBoard = false
        notifyObservers()
    }

    private fun getItems(boardId: UUID): MutableList<Item> {
        val allItems = mutableListOf<Item>()
        if (currentBoardIdx == 0) {
            runBlocking {
                for (i in 1 until boards.size) {
                    allItems += ItemService.getItems(boards[i].id).toMutableList()
                }
            }
        } else {
            runBlocking {
                allItems += ItemService.getItems(boardId).toMutableList()
            }
        }
        return allItems
    }

    fun getCurrentItems(): List<Item> {
        return currItems
    }

    fun addToDoItem(item: Item) {
        runBlocking {
            ItemService.addItem(item.boardId, item)
            //add item, notify mutators, apply the settings and then notify displaying views
            currItems = getItems(getCurrentBoard().id).toMutableList()
            notifySearchFilterSort()
            applySearchFilterSort()
            notifyObservers()
        }
    }

    fun setCreateBoardMenu(toOpen: Boolean) {
        showCreateBoard = toOpen
        notifyObservers()
    }

    fun setShowAddUserModal(toOpen: Boolean) {
        showAddUsersModalView = toOpen
        notifyObservers()
    }

    //apply all three of search, filter and sort at the same time
    //by taking the intersection of their individual sets
    private fun applySearchFilterSort() {
        val filterAndSort = currSort.intersect(currFilter.toSet())
        val searchSortAndFilter = filterAndSort.intersect(currSearch.toSet())
        currItems = searchSortAndFilter.toMutableList()
    }

    fun changeItemOrder(from: Int, to: Int) {
        runBlocking {
            currSort = ItemService.orderItem(boards[currentBoardIdx].id, from, to)
            applySearchFilterSort()
            notifyObservers()
        }
    }

    fun filterByPriorities(priorities: MutableSet<Int>, notify: Boolean = true) {
        if (currentBoardIdx == 0) {
            runBlocking {
                val allFilters = mutableListOf<Item>()
                for (i in 1 until boards.size) {
                    allFilters += ItemService.filterByPriorities(boards[i].id, priorities)
                }
                currFilter = allFilters
            }
        } else {
            runBlocking {
                currFilter = ItemService.filterByPriorities(boards[currentBoardIdx].id, priorities)
            }
        }

        //notify all observer only if we're not notifying just the mutators
        if (notify) {
            applySearchFilterSort()
            notifyObservers()
        }
    }

    fun filterByLabels(labels: MutableSet<Label>, notify: Boolean = true) {
        if (currentBoardIdx == 0) {
            runBlocking {
                val allFilters = mutableListOf<Item>()
                for (i in 1 until boards.size) {
                    allFilters += ItemService.filterByLabels(boards[i].id, labels)
                }
                currFilter = allFilters
            }
        } else {
            runBlocking {
                currFilter = ItemService.filterByLabels(boards[currentBoardIdx].id, labels)
            }
        }

        //notify all observer only if we're not notifying just the mutators
        if (notify) {
            applySearchFilterSort()
            notifyObservers()
        }
    }

    fun filterByDates(dates: Pair<LocalDate, LocalDate?>, notify: Boolean = true) {
        if (currentBoardIdx == 0) {
            runBlocking {
                val allFilters = mutableSetOf<Item>()
                for (i in 1 until boards.size) {
                    allFilters += ItemService.filterByDates(
                        boards[i].id,
                        dates.first.atStartOfDay(),
                        dates.second?.atStartOfDay()
                    )
                }
                currFilter = allFilters.sortedBy { it.dueDate }
            }
        } else {
            runBlocking {
                currFilter = ItemService.filterByDates(
                    boards[currentBoardIdx].id,
                    dates.first.atStartOfDay(),
                    dates.second?.atStartOfDay()
                )
            }
        }

        //notify all observer only if we're not notifying just the mutators
        if (notify) {
            applySearchFilterSort()
            notifyObservers()
        }
    }

    fun sortItems(sortBy: String, orderBy: String, notify: Boolean = true) {
        if (currentBoardIdx == 0) {
            runBlocking {
                val allSorted = mutableListOf<Item>()
                for (i in 1 until boards.size) {
                    allSorted += ItemService.sort(boards[i].id, sortBy, orderBy)
                }

                val mergedSorted = when (sortBy) {
                    "priority" -> {
                        if (orderBy == "DESC") {
                            allSorted.sortedByDescending { it.priority }
                        } else {
                            allSorted.sortedBy { it.priority }
                        }
                    }

                    "dueDate" -> {
                        if (orderBy == "DESC") {
                            allSorted.sortedByDescending { it.dueDate }
                        } else {
                            allSorted.sortedBy { it.dueDate }
                        }
                    }

                    else -> allSorted
                }
                currSort = mergedSorted
            }
        } else {
            print("$sortBy, $orderBy")
            runBlocking {
                currSort = ItemService.sort(getCurrentBoard().id, sortBy, orderBy)
            }
        }

        //notify all observer only if we're not notifying just the mutators
        if (notify) {
            applySearchFilterSort()
            notifyObservers()
        }
    }

    fun customOrderEnabled(): Boolean {
        return (getCurrentBoard().name != "All" && currItems == getItems(getCurrentBoard().id))
    }

    fun searchItems(searchString: String, notify: Boolean = true) {
        if (currentBoardIdx == 0) {
            runBlocking {
                val allSorted = mutableListOf<Item>()
                for (i in 1 until boards.size) {
                    allSorted += ItemService.search(boards[i].id, searchString)
                }
                currSearch = allSorted
            }
        } else {
            runBlocking {
                currSearch = ItemService.search(getCurrentBoard().id, searchString)
            }
        }

        //notify all observer only if we're not notifying just the mutators
        if (notify) {
            applySearchFilterSort()
            notifyObservers()
        }
    }

    fun logout() {
        AuthService.logout()
        app.changeScene("login")
    }

    fun updateItem(item: Item) {
        runBlocking {
            ItemService.updateItem(item.boardId, item)
            currItems = getItems(getCurrentBoard().id).toMutableList()
            notifyObservers()
        }
    }

    fun deleteItem(item: Item) {
        runBlocking {
            ItemService.deleteItem(item.boardId, item.id)
            currItems = getItems(getCurrentBoard().id).toMutableList()
            notifyObservers()
        }
    }

    fun resetSort() {
        runBlocking {
            currItems = getItems(getCurrentBoard().id).toMutableList()
            currSort = currItems
            notifyObservers()
        }
    }

    override val coroutineContext = Dispatchers.JavaFx
}
