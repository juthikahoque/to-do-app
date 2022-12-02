package frontend.utils

import frontend.Model
import frontend.app
import frontend.services.BoardService
import frontend.services.ItemService
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import kotlinx.coroutines.runBlocking
import models.Board
import models.Item

object UndoRedoManager {
    private val stacks = mutableMapOf<String, MutableList<MutableList<ApplicationSnapshot>>>()
    private lateinit var model: Model

    fun init(model: Model) {
        this.model = model
        app.addHotkey(KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN)) {
            handleUndo()
        }
        app.addHotkey(KeyCodeCombination(KeyCode.Z, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN)) {
            handleRedo()
        }
    }

    fun handleAction(action: Actions, items: List<Item>, boards: List<Board>, actionMetaData: ActionMetaData?) {
        if (stacks[model.currentBoard.value.id.toString()] == null) {
            stacks[model.currentBoard.value.id.toString()] = mutableListOf(
                mutableListOf(),
                mutableListOf()
            )
        }

        // add to undo stack
        stacks[model.currentBoard.value.id.toString()]?.get(0)?.add(
            ApplicationSnapshot(action, items.map { it }, boards.map { it }, actionMetaData)
        )

        // clear redo stack
        stacks[model.currentBoard.value.id.toString()]?.get(1)?.clear()
    }

    private fun addCurrentStateToRedoStack(state: ApplicationSnapshot) {
        val copiedItems = model.items.map { it }
        val copiedBoards = model.boards.map { it }
        stacks[model.currentBoard.value.id.toString()]?.get(1)?.add(
            ApplicationSnapshot(state.action, copiedItems, copiedBoards, state.actionMetaData)
        )
    }

    private fun addCurrentStateToUndoStack(state: ApplicationSnapshot) {
        val copiedItems = model.items.map { it }
        val copiedBoards = model.boards.map { it }
        stacks[model.currentBoard.value.id.toString()]?.get(0)?.add(
            ApplicationSnapshot(state.action, copiedItems, copiedBoards, state.actionMetaData)
        )
    }

    private fun handleUndo() {
        val boardStacks = stacks[model.currentBoard.value.id.toString()]
        if (boardStacks != null) {
            val undoStack = boardStacks[0]
            if (undoStack.isNotEmpty()) {
                val previousState = undoStack.removeLast()
                when (previousState.action) {
                    Actions.ADD_BOARD -> undoAddBoard(previousState)
                    Actions.UPDATE_BOARD  -> undoUpdateBoard(previousState)
                    Actions.ADD_ITEM -> undoAddItem(previousState)
                    Actions.UPDATE_ITEM -> undoUpdateItem(previousState)
                    Actions.DELETE_ITEM -> undoDeleteItem(previousState)
                    Actions.REORDER_ITEM -> undoItemReorder(previousState)
                    else -> println("Invalid action provided")
                }
            }
        }
    }

    private fun handleRedo() {
        val boardStacks = stacks[model.currentBoard.value.id.toString()]
        if (boardStacks != null) {
            val redoStack = boardStacks[1]
            if (redoStack.isNotEmpty()) {
                val nextState = redoStack.removeLast()
                when (nextState.action) {
                    Actions.ADD_BOARD -> redoAddBoard(nextState)
                    Actions.UPDATE_BOARD -> redoUpdateBoard(nextState)
                    Actions.ADD_ITEM -> redoAddItem(nextState)
                    Actions.UPDATE_ITEM -> redoUpdateItem(nextState)
                    Actions.DELETE_ITEM -> redoDeleteItem(nextState)
                    Actions.REORDER_ITEM -> redoItemReorder(nextState)
                    else -> println("Invalid action provided")
                }
            }
        }
    }

    private fun undoAddBoard(previousState: ApplicationSnapshot) {
        val boardToDelete = model.boards.minus(previousState.boards.toSet())
        if (boardToDelete.isNotEmpty()) {
            runBlocking {
                BoardService.deleteBoard(boardToDelete.first().id)
                addCurrentStateToRedoStack(previousState)
                model.updateBoards()
            }
        }
    }

    private fun redoAddBoard(nextState: ApplicationSnapshot) {
        val boardToAdd = nextState.boards.minus(model.boards.toSet())
        if (boardToAdd.isNotEmpty()) {
            runBlocking {
                BoardService.addBoard(boardToAdd.first())
                addCurrentStateToUndoStack(nextState)
                model.updateBoards()
            }
        }
    }

    private fun undoUpdateBoard(previousState: ApplicationSnapshot) {
        val boardToUpdate = previousState.boards.minus(model.boards.toSet())
        if (boardToUpdate.isNotEmpty()) {
            runBlocking {
                BoardService.updateBoard(boardToUpdate.first())
                addCurrentStateToRedoStack(previousState)
                model.updateBoards()
            }
        }
    }

    private fun redoUpdateBoard(nextState: ApplicationSnapshot) {
        val boardToUpdate = nextState.boards.minus(model.boards.toSet())
        if (boardToUpdate.isNotEmpty()) {
            runBlocking {
                BoardService.updateBoard(boardToUpdate.first())
                addCurrentStateToUndoStack(nextState)
                model.updateBoards()
            }
        }
    }

    private fun undoAddItem(previousState: ApplicationSnapshot) {
        val itemToDelete = model.items.minus(previousState.items.toSet())
        if (itemToDelete.isNotEmpty()) {
            runBlocking {
                ItemService.deleteItem(itemToDelete.first().boardId, itemToDelete.first().id)
                addCurrentStateToRedoStack(previousState)
                model.updateItems()
            }
        }
    }

    private fun redoAddItem(nextState: ApplicationSnapshot) {
        val itemToAdd = nextState.items.minus(model.items.toSet())
        if (itemToAdd.isNotEmpty()) {
            runBlocking {
                ItemService.addItem(itemToAdd.first().boardId, itemToAdd.first())
                addCurrentStateToUndoStack(nextState)
                model.updateItems()
            }
        }
    }

    private fun undoUpdateItem(previousState: ApplicationSnapshot) {
        val itemToUpdate = previousState.items.minus(model.items.toSet())
        if (itemToUpdate.isNotEmpty()) {
            runBlocking {
                ItemService.updateItem(itemToUpdate.first().boardId, itemToUpdate.first())
                addCurrentStateToRedoStack(previousState)
                model.updateItems()
            }
        }
    }

    private fun redoUpdateItem(nextState: ApplicationSnapshot) {
        val itemToUpdate = nextState.items.minus(model.items.toSet())
        if (itemToUpdate.isNotEmpty()) {
            runBlocking {
                ItemService.updateItem(itemToUpdate.first().boardId, itemToUpdate.first())
                addCurrentStateToUndoStack(nextState)
                model.updateItems()
            }
        }
    }

    private fun undoDeleteItem(previousState: ApplicationSnapshot) {
        val itemToAdd = previousState.items.minus(model.items.toSet())
        if (itemToAdd.isNotEmpty()) {
            runBlocking {
                ItemService.addItem(itemToAdd.first().boardId, itemToAdd.first())
                addCurrentStateToRedoStack(previousState)
                model.updateItems()
            }
        }
    }

    private fun redoDeleteItem(nextState: ApplicationSnapshot) {
        val itemToDelete = model.items.minus(nextState.items.toSet())
        if (itemToDelete.isNotEmpty()) {
            runBlocking {
                ItemService.deleteItem(itemToDelete.first().boardId, itemToDelete.first().id)
                addCurrentStateToUndoStack(nextState)
                model.updateItems()
            }
        }
    }

    private fun undoItemReorder(previousState: ApplicationSnapshot) {
        runBlocking {
            ItemService.orderItem(
                model.currentBoard.value.id,
                previousState.actionMetaData!!.to,
                previousState.actionMetaData.from
            )
            addCurrentStateToRedoStack(previousState)
            model.updateItems()
        }
    }

    private fun redoItemReorder(nextState: ApplicationSnapshot) {
        runBlocking {
            ItemService.orderItem(
                model.currentBoard.value.id,
                nextState.actionMetaData!!.from,
                nextState.actionMetaData.to
            )
            addCurrentStateToUndoStack(nextState)
            model.updateItems()
        }
    }
}
