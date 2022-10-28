package services

import kotlinx.serialization.Serializable
import models.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.time.LocalDateTime
import java.util.*

internal class ItemServiceTest {
    @BeforeEach
    fun init() {
        assertDoesNotThrow {
            val url = "jdbc:sqlite:test.db"
            conn = DriverManager.getConnection(url)
            ItemService.init(conn)
        }
    }

    @AfterEach
    fun cleanup() {
        assertDoesNotThrow {
            val stat = conn.createStatement()
            stat.executeUpdate("DROP TABLE items")
        }
    }
    @Test
    fun addItem() {
        val item = Item("todo")

        ItemService.addItem(item)

        val res = ItemService.getItem(item.id)

        assertEquals(res, item)
    }

    @Test
    fun getAllItems() {
        val item1 = Item("todo1", LocalDateTime.now(), UUID.randomUUID(), mutableSetOf(Label("label")), 1, UUID.randomUUID(), true)
        val item2 = Item("todo1", LocalDateTime.now(), item1.boardId, mutableSetOf(Label("label")), 1, UUID.randomUUID(), true)

        val items = listOf(
            item1,
            item2
        )

        items.forEach { ItemService.addItem(it) }

        val res = ItemService.getAllItems(item1.boardId)

        val it1 = items.elementAt(0)
        val it2 = items.elementAt(1)

        assertEquals(it1, item1)
        assertEquals(it2, item2)
        assertEquals(res.size, items.size)
    }

    @Test
    fun getItem() {
        val newItem = Item("item")
        val itemId = newItem.id
        ItemService.addItem(newItem)

        val itemWithId = ItemService.getItem(itemId)

        assertEquals(itemWithId, newItem)
    }

    @Test
    fun updateItem() {
        val boardId = UUID.randomUUID()
        val newItem = Item("board", LocalDateTime.now(), boardId, mutableSetOf(Label("label")), 1, UUID.randomUUID(), true)
        val updatedItem = Item("updated", LocalDateTime.now(), boardId, mutableSetOf(Label("updated")), 2, newItem.id, false)
        ItemService.addItem(newItem)

        val itemAfterUpdate = ItemService.updateItem(updatedItem)

        assertEquals(boardId, itemAfterUpdate.boardId)
        assertEquals(newItem.id, itemAfterUpdate.id)
        assertFalse(itemAfterUpdate.done)
        assertEquals(updatedItem.priority, itemAfterUpdate.priority)
        assertEquals("updated", itemAfterUpdate.text)
        assertEquals(Label("updated"), itemAfterUpdate.labels.first())
    }

    @Test
    fun deleteItem() {
        val newItem = Item("item")
        val itemId = newItem.id
        ItemService.addItem(newItem)

        val isDeletedTrue = ItemService.deleteItem(itemId)

        assertTrue(isDeletedTrue)

        val isDeletedFalse = ItemService.deleteItem(itemId)
        assertFalse(isDeletedFalse)
    }

    @Test
    fun filterByDate() {
        val items = listOf(
            Item(text = "same day", dueDate = LocalDateTime.now()),
            Item(text = "item2", dueDate = LocalDateTime.now().plusDays(1)),
            Item(text = "same day", dueDate = LocalDateTime.now().plusHours(5))
        )
        items.forEach { ItemService.addItem(it) }

        var sameDueDate = items.filter { it.text == "same day" }

        var filteredItems = ItemService.filterByDate(LocalDateTime.now())

        assertEquals(filteredItems, sameDueDate)

        sameDueDate = items.filter { it.text == "item2" }
        filteredItems = ItemService.filterByDate(LocalDateTime.now().plusDays(1))

        assertEquals(filteredItems, sameDueDate)
    }
    @Test
    fun filterByLabels() {
        val items = listOf(
            Item(text = "CS 346", priority = 0, labels = mutableSetOf(Label("CS 346"), Label("CS 341"))),
            Item(text = "CS 346", priority = 1, labels = mutableSetOf(Label("CS 346"))),
            Item(text = "CS 341", priority = 0, labels = mutableSetOf(Label("CS 341")))
        )

        items.forEach { ItemService.addItem(it) }

        var sameLabels = items.filter { it.text == "CS 346" }
        var filteredItems = ItemService.filterByLabel(Label("CS 346"))

        assertEquals(filteredItems, sameLabels)

        sameLabels = items.filter { it.priority == 0 }
        filteredItems = ItemService.filterByLabel(Label("CS 341"))

        assertEquals(filteredItems, sameLabels)
    }

    @Test
    fun filterByPriority() {
        val items = listOf(
            Item(text = "CS 346", priority = 1),
            Item(text = "CS 346", priority = 1),
            Item(text = "same day", priority = 0)
        )

        items.forEach { ItemService.addItem(it) }

        var samePriority = items.filter { it.priority == 1 }
        var filteredItems = ItemService.filterByPriority(1)

        assertEquals(filteredItems, samePriority)

        samePriority = items.filter { it.priority == 0 }
        filteredItems = ItemService.filterByPriority(0)

        assertEquals(filteredItems, samePriority)

    }

    @Test
    fun testDone() {
        val newItem = Item("item")
        ItemService.addItem(newItem)

        val done = ItemService.markItemAsDone(newItem)

        assertTrue(done)
    }
}

