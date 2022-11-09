package backend.services

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
    fun testDone() {
        val newItem = Item("item")
        ItemService.addItem(newItem)

        val done = ItemService.markItemAsDone(newItem)

        assertTrue(done)
    }
}

