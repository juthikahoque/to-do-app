package backend.services

import models.Item
import models.Label
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
        val currentDate = LocalDateTime.of(2022, 11, 11, 14, 53)
        val items = listOf(
            Item(text = "item1", dueDate = currentDate, boardId = boardId1),
            Item(text = "item2", dueDate = currentDate.plusDays(1), boardId = boardId1),
            Item(text = "item1", dueDate = currentDate.plusHours(5), boardId = boardId1),
            Item(text = "item3", dueDate = currentDate.plusDays(2), boardId = boardId2),
            Item(text = "item4", dueDate = currentDate.plusHours(5), boardId = boardId2),
        )
        items.forEach { ItemService.addItem(it) }

        // items between today and tomorrow with boardId 1
        var sameDueDate = items.filter { it.text == "item1" }
        var filteredItems = ItemService.filterByDate(LocalDateTime.now(), boardId1)
        assertEquals(sameDueDate, filteredItems)

        // items between tomorrow and the day after with boardId 1
        sameDueDate = items.filter { it.text == "item2" }
        filteredItems = ItemService.filterByDate(LocalDateTime.now().plusDays(1), boardId1)
        assertEquals(sameDueDate, filteredItems)

        // items with today's duedate and boardId 3
        filteredItems = ItemService.filterByDate(LocalDateTime.now(), boardId3)
        assertEquals(filteredItems.size, 0)

        // all items with boardId1
        sameDueDate = listOf(items[0], items[2], items[1])
        filteredItems = ItemService.filterByDate(LocalDateTime.now(), boardId1, LocalDateTime.now().plusDays(3))
        assertEquals(sameDueDate, filteredItems)

        // all items with boardId 2
        sameDueDate = listOf(items[4], items[3])
        filteredItems = ItemService.filterByDate(LocalDateTime.now(), boardId2, LocalDateTime.now().plusDays(3))
        assertEquals(sameDueDate, filteredItems)

    }
    @Test
    fun filterByLabels() {
        val boardId1 = UUID.randomUUID()
        val boardId2 = UUID.randomUUID()
        val items = listOf(
            Item(text = "CS 346", priority = 2, labels = mutableSetOf(Label("CS 341"), Label("CS 346")), boardId = boardId1),
            Item(text = "CS 346", priority = 1, labels = mutableSetOf(Label("CS 346")), boardId = boardId1),
            Item(text = "CS 341", priority = 0, labels = mutableSetOf(Label("CS 341")), boardId = boardId1),
            Item(text = "CS 341", priority = 1, labels = mutableSetOf(Label("CS 341")), boardId = boardId2)
        )

        items.forEach { ItemService.addItem(it) }

        // filtering for board 1 with CS 346 label
        var sameLabels = items.filter { it.text == "CS 346" }
        var filteredItems = ItemService.filterByLabel(mutableSetOf(Label("CS 346")), boardId1)
        assertEquals(filteredItems, sameLabels)

        // filtering for board 1 with CS 341 label
        sameLabels = items.filter { it.priority == 0 || it.priority == 2 }
        filteredItems = ItemService.filterByLabel(mutableSetOf(Label("CS 341")), boardId1)
        assertEquals(filteredItems, sameLabels)

        // filtering for board 1 with both labels
        sameLabels = listOf(items[0], items[2], items[1])
        filteredItems = ItemService.filterByLabel(mutableSetOf(Label("CS 346"), Label("CS 341")), boardId1)
        assertEquals(filteredItems, sameLabels)

        // filtering for board 2
        sameLabels = items.filter { it.boardId == boardId2 }
        filteredItems = ItemService.filterByLabel(mutableSetOf(Label("CS 341")), boardId2)
        assertEquals(filteredItems, sameLabels)

        // filtering for board 1 with both labels, sorted by priority
        sameLabels = listOf(items[2], items[1], items[0])
        filteredItems = ItemService.filterByLabel(mutableSetOf(Label("CS 346"), Label("CS 341")), boardId1, "priority")
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

        // filter by priority 1 in board 1
        var samePriority = items.filter { it.priority == 1 && it.boardId == boardId1 }
        var filteredItems = ItemService.filterByPriority(mutableSetOf(1), boardId1)
        assertEquals(samePriority, filteredItems)

        // filter by priority 0 in board 1
        samePriority = items.filter { it.priority == 0 && it.boardId == boardId1 }
        filteredItems = ItemService.filterByPriority(mutableSetOf(0), boardId1)
        assertEquals(samePriority, filteredItems)

        // filter by both priorities and sort by priority
        samePriority = listOf(items[2], items[0], items[1])
        filteredItems = ItemService.filterByPriority(mutableSetOf(1, 0), boardId1)
        assertEquals(samePriority, filteredItems)

        // filter for board 2 priorities
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

    private fun assertOrdering(names: List<String>, items: List<Item>) {
        assertEquals(names.size, items.size)
        items.forEachIndexed { idx, ele -> assertEquals(names[idx], ele.text)}
    }
    @Test
    fun changeOrder() {
        val boardId = UUID.randomUUID()
        val items = listOf(
            Item("1", boardId=boardId),
            Item("2", boardId=boardId),
            Item("3", boardId=boardId),
        )
        items.forEach { ItemService.addItem(it) }

        assertOrdering(listOf("1", "2", "3"), ItemService.getAllItems(boardId))

        ItemService.changeOrder(boardId, 0, 2)

        assertOrdering(listOf("2", "3", "1"), ItemService.getAllItems(boardId))
    }

    @Test
    fun sorting() {
        val boardId1 = UUID.randomUUID()
        val boardId2 = UUID.randomUUID()
        val items = listOf(
            Item(text = "CS 346", priority = 0, dueDate = LocalDateTime.now(), labels = mutableSetOf(Label("CS 346"), Label("CS 341")), boardId = boardId1),
            Item(text = "CS 346", priority = 1, dueDate = LocalDateTime.now().plusDays(1), labels = mutableSetOf(Label("CS 346")), boardId = boardId1),
            Item(text = "CS 341", priority = 0, dueDate = LocalDateTime.now().plusHours(1), labels = mutableSetOf(Label("CS 341")), boardId = boardId1),
            Item(text = "CS 341", priority = 1, dueDate = LocalDateTime.now(), labels = mutableSetOf(Label("CS 341")), boardId = boardId2)
        )

        items.forEach{ ItemService.addItem(it) }

        var res = ItemService.sortByDueDates(boardId1, "ASC")

        assertEquals(res.size, 3)
        assertEquals(res[0], items[0])
        assertEquals(res[1], items[2])
        assertEquals(res[2], items[1])

        res = ItemService.sortByDueDates(boardId1, "DESC")
        assertEquals(res.size, 3)
        assertEquals(res[0], items[1])
        assertEquals(res[1], items[2])
        assertEquals(res[2], items[0])

        res = ItemService.sortByLabels(boardId1, "ASC")
        assertEquals(res.size, 3)
        assertEquals(res[0], items[0])
        assertEquals(res[1], items[2])
        assertEquals(res[2], items[1])

        res = ItemService.sortByLabels(boardId1, "DESC")
        assertEquals(res.size, 3)
        assertEquals(res[0], items[0])
        assertEquals(res[1], items[1])
        assertEquals(res[2], items[2])

        res = ItemService.sortByPriority(boardId1, "ASC")
        assertEquals(res.size, 3)
        assertEquals(res[0], items[0])
        assertEquals(res[1], items[2])
        assertEquals(res[2], items[1])

        res = ItemService.sortByPriority(boardId1, "DESC")
        assertEquals(res.size, 3)
        assertEquals(res[0], items[1])
        assertEquals(res[1], items[0])
        assertEquals(res[2], items[2])

    }
}

