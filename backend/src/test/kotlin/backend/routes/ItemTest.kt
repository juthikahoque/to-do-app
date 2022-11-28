package backend.routes

import backend.services.Database
import backend.services.ItemService
import backend.services.conn
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import models.Item
import models.Label
import models.User
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class ItemTest {
    private val user = User("rIliX3UCwhY7qvdPeh0jJsQL1UR2", "test", "test@email.com")
    @BeforeEach
    fun init() {
        Assertions.assertDoesNotThrow {
            conn = Database().connect("test")

            // clear table
            val stat = conn.createStatement()
            stat.executeUpdate("DROP TABLE IF EXISTS items")
            stat.executeUpdate("DROP TABLE IF EXISTS items_labels")

            ItemService.init(conn)
        }
    }

    @AfterEach
    fun cleanup() {
        Assertions.assertDoesNotThrow {
            val stat = conn.createStatement()
            stat.executeUpdate("DROP TABLE items")
            stat.executeUpdate("DROP TABLE items_labels")
        }
    }

    @Test
    fun testDueDateRoute() = testApplication {

        val boardId1 = UUID.randomUUID()
        val currentDate = LocalDateTime.of(2022, 11, 11, 14, 53)
        val items = listOf(
            Item(title = "item1", dueDate = currentDate, boardId = boardId1, owner = user),
            Item(title = "item2", dueDate = currentDate.plusDays(1), boardId = boardId1, owner = user),
            Item(title = "item1", dueDate = currentDate.plusHours(5), boardId = boardId1, owner = user),
        )
        items.forEach { ItemService.addItem(it) }

        // items between today and tomorrow with boardId 1
        val sameDueDate = items.filter { it.title == "item1" }

        val client = configureTest("duedate")
        var headers = Parameters.build {
            append("date", currentDate.toString())
            append("date", currentDate.plusDays(1).toString())
        }.formUrlEncode()

        var result = client.get("board/${boardId1}/items?${headers}")

        assertEquals(HttpStatusCode.OK, result.status)
        assertEquals(sameDueDate, result.body<List<Item>>())

        headers = Parameters.build {
            append("date", currentDate.toString())
            append("date", "")
        }.formUrlEncode()

        result = client.get("board/${boardId1}/items?${headers}")

        assertEquals(HttpStatusCode.OK, result.status)
        assertEquals(sameDueDate, result.body<List<Item>>())

    }

    @Test
    fun testPriorityRoute() = testApplication {
        val boardId1 = UUID.randomUUID()
        val items = listOf(
            Item(title = "CS 346", priority = 1, boardId = boardId1, owner = user),
            Item(title = "CS 346", priority = 1, boardId = boardId1, owner = user),
            Item(title = "same day", priority = 0, boardId = boardId1, owner = user),
        )

        items.forEach { ItemService.addItem(it) }

        val samePriority = items.filter { it.priority == 1 }

        val client = configureTest("priority")
        val headers = Parameters.build {
            append("priority", "1")
        }.formUrlEncode()

        val result = client.get("board/${boardId1}/items?${headers}")

        assertEquals(HttpStatusCode.OK, result.status)
        assertEquals(samePriority, result.body<List<Item>>())

    }

    @Test
    fun testLabelsRoute() = testApplication {
        val boardId1 = UUID.randomUUID()
        val items = listOf(
            Item(
                title = "CS 346",
                priority = 1,
                labels = mutableSetOf(Label("CS 346")),
                boardId = boardId1,
                owner = user
            ),
            Item(
                title = "CS 346",
                priority = 0,
                labels = mutableSetOf(Label("CS 346"), Label("CS 341")),
                boardId = boardId1,
                owner = user
            ),
            Item(
                title = "CS 341",
                priority = 0,
                labels = mutableSetOf(Label("CS 341")),
                boardId = boardId1,
                owner = user
            ),
        )

        items.forEach { ItemService.addItem(it) }

        val sameLabels = listOf(items[1], items[0])

        val client = configureTest("labels")
        val headers = Parameters.build {
            append("label", "CS 346")
            append("sortBy", "priority")
        }.formUrlEncode()

        val result = client.get("board/${boardId1}/items?${headers}")

        assertEquals(HttpStatusCode.OK, result.status)
        assertEquals(sameLabels, result.body<List<Item>>())

    }

    @Test
    fun testSorting() = testApplication {
        val boardId1 = UUID.randomUUID()
        val items = listOf(
            Item(
                title = "CS 346",
                priority = 0,
                dueDate = LocalDateTime.now(),
                labels = mutableSetOf(Label("CS 346"), Label("CS 341")),
                boardId = boardId1,
                owner = user
            ),
            Item(
                title = "CS 346",
                priority = 1,
                dueDate = LocalDateTime.now().plusDays(1),
                labels = mutableSetOf(Label("CS 346")),
                boardId = boardId1,
                owner = user
            ),
            Item(
                title = "CS 341",
                priority = 0,
                dueDate = LocalDateTime.now().plusHours(1),
                labels = mutableSetOf(Label("CS 341")),
                boardId = boardId1,
                owner = user
            ),
        )

        items.forEach { ItemService.addItem(it) }

        val client = configureTest("sorting")

        var headers = Parameters.build {
            append("sortBy", "priority")
        }.formUrlEncode()
        var expected = listOf(items[0], items[2], items[1])
        var result = client.get("board/${boardId1}/items?${headers}")
        assertEquals(HttpStatusCode.OK, result.status)
        assertEquals(expected, result.body<List<Item>>())

        headers = Parameters.build {
            append("sortBy", "priority")
            append("orderBy", "DESC")
        }.formUrlEncode()
        expected = listOf(items[1], items[0], items[2])
        result = client.get("board/${boardId1}/items?${headers}")
        assertEquals(HttpStatusCode.OK, result.status)
        assertEquals(expected, result.body<List<Item>>())

    }

    @Test
    fun testSearch() = testApplication {
        val boardId1 = UUID.randomUUID()
        val items = listOf(
            Item(title = "item1", boardId = boardId1, owner = user),
            Item(title = "item2", boardId = boardId1, owner = user),
            Item(title = "item1", boardId = boardId1, owner = user)
        )
        items.forEach { ItemService.addItem(it) }

        val client = configureTest("searching")

        val headers = Parameters.build {
            append("search", "item1")
        }.formUrlEncode()

        val expectedResult = items.filter { it.title == "item1" }
        val result = client.get("board/${boardId1}/items?${headers}")

        assertEquals(HttpStatusCode.OK, result.status)
        assertEquals(expectedResult, result.body<List<Item>>())

    }

}