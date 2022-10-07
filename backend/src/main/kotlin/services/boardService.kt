package services

import java.util.UUID
import models.*

class BoardService() {

    val boards = mutableSetOf(Board("hello"), Board("world"))

    fun addBoard(board: Board): Board {
        // assert board
        boards.add(board)
        return board
    }

    fun getBoards(): List<Board> {
        return boards.toList()
    }

    fun getBoard(id: UUID): Board {
        return boards.first({ it.id == id })
    }

    fun updateBoard(new: Board): Board? {
        val board = boards.firstOrNull() { it.id == new.id }
        if (board == null) {
            return null
        }
        boards.remove(board)
        boards.add(new)
        return new
    }

    fun deleteBoard(id: UUID) {
        boards.removeIf({it.id == id})
    }
}