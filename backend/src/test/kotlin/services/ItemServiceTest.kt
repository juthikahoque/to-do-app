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
        val boardId1 = UUID.randomUUID()
        val boardId2 = UUID.randomUUID()
        val boardId3 = UUID.randomUUID()
        val items = listOf(
            Item(text = "item1", dueDate = LocalDateTime.now(), boardId = boardId1),
            Item(text = "item2", dueDate = LocalDateTime.now().plusDays(1), boardId = boardId1),
            Item(text = "item1", dueDate = LocalDateTime.now().plusHours(5), boardId = boardId1),
            Item(text = "item3", dueDate = LocalDateTime.now().plusDays(2), boardId = boardId2),
            Item(text = "item4", dueDate = LocalDateTime.now().plusHours(5), boardId = boardId2),
        )
        items.forEach { ItemService.addItem(it) }

        // items between today and tomorrow with boardId 1
        var sameDueDate = items.filter { it.text == "item1" }
        var filteredItems = ItemService.filterByDate(LocalDateTime.now(), boardId1)
        assertEquals(filteredItems, sameDueDate)

        // items between tomorrow and the day after with boardId 1
        sameDueDate = items.filter { it.text == "item2" }
        filteredItems = ItemService.filterByDate(LocalDateTime.now().plusDays(1), boardId1)
        assertEquals(filteredItems, sameDueDate)

        // items with today's duedate and boardId 3
        filteredItems = ItemService.filterByDate(LocalDateTime.now(), boardId3)
        assertEquals(filteredItems.size, 0)

        // all items with boardId1
        sameDueDate = items.filter { it.text == "item2" || it.text == "item1" }
        filteredItems = ItemService.filterByDate(LocalDateTime.now(), boardId1, LocalDateTime.now().plusDays(3))
        assertEquals(sameDueDate, filteredItems)

        // all items with boardId 2
        sameDueDate = items.filter { it.text == "item3" || it.text == "item4" }
        filteredItems = ItemService.filterByDate(LocalDateTime.now(), boardId2, LocalDateTime.now().plusDays(3))
        assertEquals(sameDueDate, filteredItems)

    }
    @Test
    fun filterByLabels() {
        val boardId1 = UUID.randomUUID()
        val boardId2 = UUID.randomUUID()
        val items = listOf(
            Item(text = "CS 346", priority = 0, labels = mutableSetOf(Label("CS 346"), Label("CS 341")), boardId = boardId1),
            Item(text = "CS 346", priority = 1, labels = mutableSetOf(Label("CS 346")), boardId = boardId1),
            Item(text = "CS 341", priority = 0, labels = mutableSetOf(Label("CS 341")), boardId = boardId1),
            Item(text = "CS 341", priority = 1, labels = mutableSetOf(Label("CS 341")), boardId = boardId2)
        )

        items.forEach { ItemService.addItem(it) }

        var sameLabels = items.filter { it.text == "CS 346" }
        var filteredItems = ItemService.filterByLabel(mutableSetOf(Label("CS 346")), boardId1)
        assertEquals(filteredItems, sameLabels)

        sameLabels = items.filter { it.priority == 0 }
        filteredItems = ItemService.filterByLabel(mutableSetOf(Label("CS 341")), boardId1)
        assertEquals(filteredItems, sameLabels)

        sameLabels = items.filter { it.boardId == boardId1 }
        filteredItems = ItemService.filterByLabel(mutableSetOf(Label("CS 346"), Label("CS 341")), boardId1)
        assertEquals(filteredItems, sameLabels)

        sameLabels = items.filter { it.boardId == boardId2 }
        filteredItems = ItemService.filterByLabel(mutableSetOf(Label("CS 341")), boardId2)
        assertEquals(filteredItems, sameLabels)

    }

    @Test
    fun filterByPriority() {
        val boardId1 = UUID.randomUUID()
        val boardId2 = UUID.randomUUID()
        val items = listOf(
            Item(text = "CS 346", priority = 1, boardId = boardId1),
            Item(text = "CS 346", priority = 1, boardId = boardId1),
            Item(text = "same day", priority = 0, boardId = boardId1),
            Item(text = "same day", priority = 0, boardId = boardId2),
        )

        items.forEach { ItemService.addItem(it) }

        var samePriority = items.filter { it.priority == 1 && it.boardId == boardId1 }
        var filteredItems = ItemService.filterByPriority(mutableSetOf(1), boardId1)
        assertEquals(filteredItems, samePriority)

        samePriority = items.filter { it.priority == 0 && it.boardId == boardId1 }
        filteredItems = ItemService.filterByPriority(mutableSetOf(0), boardId1)
        assertEquals(filteredItems, samePriority)

        samePriority = items.filter { it.boardId == boardId1 }
        filteredItems = ItemService.filterByPriority(mutableSetOf(1, 0), boardId1)
        assertEquals(filteredItems, samePriority)

        samePriority = items.filter { it.boardId == boardId2 }
        filteredItems = ItemService.filterByPriority(mutableSetOf(0), boardId2)
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

