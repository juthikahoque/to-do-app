package services

import models.Board
import models.Label
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class BoardServiceTest {

    @Test
    fun addBoard() {
        val boardService = BoardService()
        val boardLen = boardService.boards.size
        val board = Board("board")

        boardService.addBoard(board)

        assertTrue(boardService.boards.contains(board))
        assertEquals(boardLen + 1, boardService.boards.size)
    }

    @Test
    fun getBoards() {
        val boardService = BoardService()
        boardService.boards.addAll(mutableSetOf(
            Board("hello"),
            Board("world"),
        ))
        val boardLen = boardService.boards.size

        val boards = boardService.getBoards()

        assertEquals(boardLen, boards.size)
    }

    @Test
    fun getBoard() {
        val boardService = BoardService()
        val board = Board("board")
        val id = board.id
        boardService.addBoard(board)

        val got = boardService.getBoard(id)

        assertEquals(board, got)
    }

    @Test
    fun updateBoard() {
        val boardId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val boardService = BoardService()
        val board = Board("board", mutableSetOf(UUID.randomUUID()), boardId, mutableSetOf(Label("label")))
        val updated = Board("updated", mutableSetOf(userId), boardId, mutableSetOf(Label("updated")))
        boardService.addBoard(board)

        val new = boardService.updateBoard(updated)!! // throws if not null

        assertEquals(boardId, new.id)
        assertEquals(userId, new.users.first())
        assertEquals("updated", new.name)
        assertEquals(Label("updated"), new.labels.first())
    }

    @Test
    fun deleteBoard() {
        val boardService = BoardService()
        val board = Board("board")
        val id = board.id
        boardService.addBoard(board)

        val res = boardService.deleteBoard(id)

        assertTrue(res)
        assertFalse(boardService.boards.contains(board))

        val res2 = boardService.deleteBoard(id)

        assertFalse(res2)
    }
}