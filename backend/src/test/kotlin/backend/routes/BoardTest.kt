package backend.routes

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import models.Board
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import backend.services.*
import models.User

class BoardTest {

    val users = mutableSetOf(User("rIliX3UCwhY7qvdPeh0jJsQL1UR2", "test", "test@email.com"))

    @BeforeEach
    fun init() {
        Assertions.assertDoesNotThrow {
            conn = Database().connect("test")
            // clear table
            val stat = conn.createStatement()
            stat.executeUpdate("DROP TABLE IF EXISTS boards")
            stat.executeUpdate("DROP TABLE IF EXISTS boards_users")
            stat.executeUpdate("DROP TABLE IF EXISTS boards_labels")

            BoardService.init(conn)
        }
    }

    @Test
    fun testGet() = testApplication {
        val client = configureTest("rIliX3UCwhY7qvdPeh0jJsQL1UR2")

        val boards = listOf(
            Board("1", users),
            Board("2", users),
        )
        boards.forEach { BoardService.addBoard(it) }

        val response = client.get("/board")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testGetById() = testApplication {
        val client = configureTest("rIliX3UCwhY7qvdPeh0jJsQL1UR2")

        val boards = listOf(
            Board("1", users),
            Board("2", users),
        )
        boards.forEach { BoardService.addBoard(it) }

        val response = client.get("/board/${boards.last().id}")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(boards.last(), response.body<Board>())
    }

    @Test
    fun testPost() = testApplication {
        val client = configureTest("user")

        val board = Board("1", users)

        val response = client.post("/board") {
            contentType(ContentType.Application.Json)
            setBody(board)
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(board, BoardService.getBoard(board.id))
    }

    @Test
    fun testUpdate() = testApplication {
        val client = configureTest("user")

        val board = Board("1", users)
        val new = board.copy(name = "new")

        // not inserted yet, should return NotFound
        val notFound = client.put("/board") {
            contentType(ContentType.Application.Json)
            setBody(new)
        }

        assertEquals(HttpStatusCode.NotFound, notFound.status)

        BoardService.addBoard(board)

        val response = client.put("/board") {
            contentType(ContentType.Application.Json)
            setBody(new)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(new, response.body<Board>())
        assertEquals(new, BoardService.getBoard(board.id))
    }

    @Test
    fun testDelete() = testApplication {
        val client = configureTest("user")

        val board = Board("1", users)
        BoardService.addBoard(board)

        val response = client.delete("/board/${board.id}")

        assertEquals(HttpStatusCode.NoContent, response.status)

        val res = client.get("/board/${board.id}")

        assertEquals(HttpStatusCode.NotFound, res.status)
    }
}