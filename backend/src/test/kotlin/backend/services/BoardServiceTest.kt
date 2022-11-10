package backend.services

import models.Board
import models.Label
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
            // clear table
            val stat = conn.createStatement()
            stat.executeUpdate("DROP TABLE IF EXISTS boards")
            stat.executeUpdate("DROP TABLE IF EXISTS boards_users")
            stat.executeUpdate("DROP TABLE IF EXISTS boards_labels")

            BoardService.init(conn)
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
        val users = mutableSetOf("user")
        val boards = listOf(
            Board("1", users),
            Board("2", users),
        )
        boards.forEach { BoardService.addBoard(it) }

        val res = BoardService.getBoards(users.first())

        assertEquals(boards, res)
    }

    @Test
    fun getBoard() {
        val users = mutableSetOf("user")
        val labels = mutableSetOf(Label("label1"), Label("label2"))
        val boards = listOf(
            Board("1", users),
            Board("2", users),
            Board("3", users, labels)
        )
        boards.forEach { BoardService.addBoard(it) }

        val res = BoardService.getBoard(boards.last().id)

        assertEquals(boards.last(), res)
    }

    @Test
    fun updateBoard() {
        val users = mutableSetOf("user")
        val boards = listOf(
            Board("1", users),
            Board("2", users),
        )
        boards.forEach { BoardService.addBoard(it) }

        val new = boards.first().copy(
            name = "new",
            updated_at = LocalDateTime.now(),
        )

        val res = BoardService.updateBoard(new)

        assertEquals(new, res)
    }

    @Test
    fun deleteBoard() {
        val users = mutableSetOf("user")
        val board = Board("board", users)
        val id = board.id
        BoardService.addBoard(board)

        val res = BoardService.deleteBoard(id)

        assertTrue(res)
        assertFalse(BoardService.getBoards(users.first()).contains(board))

        val res2 = BoardService.deleteBoard(id)

        assertFalse(res2)
    }
}