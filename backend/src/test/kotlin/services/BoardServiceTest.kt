package services

import models.Board
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.time.LocalDateTime

lateinit var conn: Connection
internal class BoardServiceTest {
    @BeforeEach
    fun init() {
        assertDoesNotThrow {
            val url = "jdbc:sqlite:test.db"
            conn = DriverManager.getConnection(url)
            BoardService.init(conn)
        }
    }

    @AfterEach
    fun cleanup() {
        assertDoesNotThrow {
            val stat = conn.createStatement()
            stat.executeUpdate("DROP TABLE boards")
        }
    }

    @Test
    fun initOk() {
        assertTrue(true)
    }

    @Test
    fun addBoard() {
        val board = Board("board")

        val res = BoardService.addBoard(board)

        assertEquals(res, board)
    }

    @Test
    fun getBoards() {
        val boards = listOf(
            Board("1"),
            Board("2"),
        )
        boards.forEach { BoardService.addBoard(it) }

        val res = BoardService.getBoards()

        assertEquals(boards, res)
    }

    @Test
    fun getBoard() {
        val boards = listOf(
            Board("1"),
            Board("2"),
        )
        boards.forEach { BoardService.addBoard(it) }

        val res = BoardService.getBoard(boards.first().id)

        assertEquals(boards.first(), res)
    }

    @Test
    fun updateBoard() {
        val boards = listOf(
            Board("1"),
            Board("2"),
        )
        boards.forEach { BoardService.addBoard(it) }

        val new = boards.first().copy(
            name = "new",
//            users =  mutableSetOf(UUID.randomUUID()),
//            labels = mutableSetOf(Label("label")),
            updated_at = LocalDateTime.now(),
        )

        val res = BoardService.updateBoard(new)

        assertEquals(new, res)
    }

    @Test
    fun deleteBoard() {
        val board = Board("board")
        val id = board.id
        BoardService.addBoard(board)

        val res = BoardService.deleteBoard(id)

        assertTrue(res)
        assertFalse(BoardService.getBoards().contains(board))

        val res2 = BoardService.deleteBoard(id)

        assertFalse(res2)
    }
}