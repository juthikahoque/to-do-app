package backend.services

import backend.services.ItemService.getItems
import models.Attachment
import models.Item
import models.Label
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
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
        val item = Item("todo", owner = user)

        ItemService.addItem(item)

        val res = ItemService.getItem(item.id)

        assertEquals(res, item)
    }

    @Test
    fun getAllItems() {
        val item1 = Item(
            "todo1",
            LocalDateTime.now(),
            UUID.randomUUID(),
            setOf(Label("label")),
            1,
            UUID.randomUUID(),
            true,
            owner = user
        )
        val item2 = Item(
            "todo1",
            LocalDateTime.now(),
            item1.boardId,
            setOf(Label("label")),
            1,
            UUID.randomUUID(),
            true,
            owner = user
        )

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
        val newItem = Item("item", owner = user)
        val itemId = newItem.id
        ItemService.addItem(newItem)

        val itemWithId = ItemService.getItem(itemId)

        assertEquals(itemWithId, newItem)
    }

    @Test
    fun updateItem() {
        val boardId = UUID.randomUUID()
        val newItem = Item(
            "board",
            LocalDateTime.now(),
            boardId,
            setOf(Label("label")),
            1,
            UUID.randomUUID(),
            true,
            owner = user
        )
        val updatedItem = Item(
            "updated",
            LocalDateTime.now(),
            boardId,
            setOf(Label("updated")),
            2,
            newItem.id,
            false,
            owner = user
        )
        ItemService.addItem(newItem)

        val itemAfterUpdate = ItemService.updateItem(updatedItem)

        assertEquals(boardId, itemAfterUpdate.boardId)
        assertEquals(newItem.id, itemAfterUpdate.id)
        assertFalse(itemAfterUpdate.done)
        assertEquals(updatedItem.priority, itemAfterUpdate.priority)
        assertEquals("updated", itemAfterUpdate.title)
        assertEquals(Label("updated"), itemAfterUpdate.labels.first())
    }

    @Test
    fun deleteItem() {
        val newItem = Item("item", owner = user)
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
            Item(title = "item1", dueDate = currentDate, boardId = boardId1, owner = user),
            Item(title = "item2", dueDate = currentDate.plusDays(1), boardId = boardId1, owner = user),
            Item(title = "item1", dueDate = currentDate.plusHours(5), boardId = boardId1, owner = user),
            Item(title = "item3", dueDate = currentDate.plusDays(2), boardId = boardId2, owner = user),
            Item(title = "item4", dueDate = currentDate.plusHours(5), boardId = boardId2, owner = user),
        )
        items.forEach { ItemService.addItem(it) }

        // items between today and tomorrow with boardId 1
        var sameDueDate = items.filter { it.title == "item1" }
        var filteredItems = getItems(
            boardId = boardId1.toString(),
            "",
            startDateTime = currentDate,
            null,
            setOf(),
            setOf(),
            null,
            actualSortBy = "dueDate",
            actualOrderBy = "ASC"
        )
        assertEquals(sameDueDate, filteredItems)

        // items between tomorrow and the day after with boardId 1
        sameDueDate = items.filter { it.title == "item2" }
        filteredItems = getItems(
            boardId1.toString(),
            "",
            currentDate.plusDays(1),
            null,
            setOf(),
            setOf(),
            null,
            "dueDate",
            "ASC"
        )
        assertEquals(sameDueDate, filteredItems)

        // items with today's duedate and boardId 3
        filteredItems = getItems(
            boardId3.toString(),
            "",
            currentDate,
            null,
            setOf(),
            setOf(),
            null,
            "dueDate",
            "ASC"
        )
        assertEquals(filteredItems.size, 0)

        // all items with boardId1
        sameDueDate = listOf(items[0], items[2], items[1])
        filteredItems = getItems(
            boardId1.toString(),
            "",
            currentDate,
            currentDate.plusDays(3),
            setOf(),
            setOf(),
            null,
            "dueDate",
            "ASC"
        )
        assertEquals(sameDueDate, filteredItems)

        // all items with boardId 2
        sameDueDate = listOf(items[4], items[3])
        filteredItems = getItems(
            boardId2.toString(),
            "",
            currentDate,
            currentDate.plusDays(3),
            setOf(),
            setOf(),
            null,
            "dueDate",
            "ASC"
        )
        assertEquals(sameDueDate, filteredItems)

    }

    @Test
    fun filterByLabels() {
        val boardId1 = UUID.randomUUID()
        val boardId2 = UUID.randomUUID()
        val items = listOf(
            Item(
                title = "CS 346",
                priority = 2,
                labels = setOf(Label("CS 341"), Label("CS 346")),
                boardId = boardId1,
                owner = user,
            ),
            Item(
                title = "CS 346",
                priority = 1,
                labels = setOf(Label("CS 346")),
                boardId = boardId1,
                owner = user,
            ),
            Item(
                title = "CS 341",
                priority = 0,
                labels = setOf(Label("CS 341")),
                boardId = boardId1,
                owner = user,
            ),
            Item(
                title = "CS 341",
                priority = 1,
                labels = setOf(Label("CS 341")),
                boardId = boardId2,
                owner = user,
            )
        )

        items.forEach { ItemService.addItem(it) }

        // filtering for board 1 with CS 346 label
        var sameLabels = items.filter { it.labels.contains(Label("CS 346")) }
        var filteredItems = getItems(
            boardId1.toString(),
            "",
            null,
            null,
            setOf(Label("CS 346")),
            setOf(),
            null,
            "label",
            "ASC"
        )
        assertEquals(sameLabels, filteredItems)

        // filtering for board 1 with CS 341 label
        sameLabels = items.filter { it.boardId == boardId1 && it.labels.contains(Label("CS 341")) }
        filteredItems = getItems(
            boardId1.toString(),
            "",
            null,
            null,
            setOf(Label("CS 341")),
            setOf(),
            null,
            "label",
            "ASC"
        )
        assertEquals(sameLabels, filteredItems)

        // filtering for board 1 with both labels
        sameLabels = listOf(items[0], items[2], items[1])
        filteredItems = getItems(
            boardId1.toString(),
            "",
            null,
            null,
            setOf(Label("CS 346"), Label("CS 341")),
            setOf(),
            null,
            "label",
            "ASC"
        )
        assertEquals(sameLabels, filteredItems)

        // filtering for board 2
        sameLabels = items.filter { it.boardId == boardId2 }
        filteredItems = getItems(
            boardId2.toString(),
            "",
            null,
            null,
            setOf(Label("CS 341")),
            setOf(),
            null,
            "label",
            "ASC"
        )
        assertEquals(sameLabels, filteredItems)

        // filtering for board 1 with both labels, sorted by priority
        sameLabels = listOf(items[2], items[1], items[0])
        filteredItems = getItems(
            boardId1.toString(),
            "",
            null,
            null,
            setOf(Label("CS 346"), Label("CS 341")),
            setOf(),
            null,
            "priority",
            "ASC"
        )
        assertEquals(sameLabels, filteredItems)
    }

    @Test
    fun filterByPriority() {
        val boardId1 = UUID.randomUUID()
        val boardId2 = UUID.randomUUID()
        val items = listOf(
            Item(title = "CS 346", priority = 1, boardId = boardId1, owner = user),
            Item(title = "CS 346", priority = 1, boardId = boardId1, owner = user),
            Item(title = "same day", priority = 0, boardId = boardId1, owner = user),
            Item(title = "same day", priority = 0, boardId = boardId2, owner = user),
        )

        items.forEach { ItemService.addItem(it) }

        // filter by priority 1 in board 1
        var samePriority = items.filter { it.priority == 1 && it.boardId == boardId1 }
        var filteredItems = getItems(
            boardId1.toString(),
            "",
            null,
            null,
            setOf(),
            setOf(1),
            null,
            "priority",
            "ASC"
        )
        assertEquals(samePriority, filteredItems)

        // filter by priority 0 in board 1
        samePriority = items.filter { it.priority == 0 && it.boardId == boardId1 }
        filteredItems = getItems(
            boardId1.toString(),
            "",
            null,
            null,
            setOf(),
            setOf(0),
            null,
            "priority",
            "ASC"
        )
        assertEquals(samePriority, filteredItems)

        // filter by both priorities and sort by priority
        samePriority = listOf(items[2], items[0], items[1])
        filteredItems = getItems(
            boardId1.toString(),
            "",
            null,
            null,
            setOf(),
            setOf(1, 0),
            null,
            "priority",
            "ASC"
        )
        assertEquals(samePriority, filteredItems)

        // filter for board 2 priorities
        samePriority = items.filter { it.boardId == boardId2 }
        filteredItems = getItems(
            boardId2.toString(),
            "",
            null,
            null,
            setOf(),
            setOf(0),
            null,
            "priority",
            "ASC"
        )
        assertEquals(samePriority, filteredItems)

    }

    @Test
    fun testDone() {
        val newItem = Item("item", owner = user)
        ItemService.addItem(newItem)

        val done = ItemService.markItemAsDone(newItem)

        assertTrue(done)
    }

    private fun assertOrdering(names: List<String>, items: List<Item>) {
        assertEquals(names.size, items.size)
        items.forEachIndexed { idx, ele -> assertEquals(names[idx], ele.title) }
    }

    @Test
    fun changeOrder() {
        val boardId = UUID.randomUUID()
        val items = listOf(
            Item("1", boardId = boardId, owner = user),
            Item("2", boardId = boardId, owner = user),
            Item("3", boardId = boardId, owner = user),
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
            Item(
                title = "CS 346",
                priority = 0,
                dueDate = LocalDateTime.now(),
                labels = setOf(Label("CS 346"), Label("CS 341")),
                boardId = boardId1,
                owner = user,
            ),
            Item(
                title = "CS 346",
                priority = 1,
                dueDate = LocalDateTime.now().plusDays(1),
                labels = setOf(Label("CS 346")),
                boardId = boardId1,
                owner = user,
            ),
            Item(
                title = "CS 341",
                priority = 0,
                dueDate = LocalDateTime.now().plusHours(1),
                labels = setOf(Label("CS 341")),
                boardId = boardId1,
                owner = user,
            ),
            Item(
                title = "CS 341",
                priority = 1,
                dueDate = LocalDateTime.now(),
                labels = setOf(Label("CS 341")),
                boardId = boardId2,
                owner = user,
            )
        )

        items.forEach { ItemService.addItem(it) }

        var res = getItems(
            boardId1.toString(),
            "",
            null,
            null,
            setOf(),
            setOf(),
            null,
            "dueDate",
            "ASC"
        )

        assertEquals(res.size, 3)
        assertEquals(items[0], res[0])
        assertEquals(items[2], res[1])
        assertEquals(items[1], res[2])

        res = getItems(
            boardId1.toString(),
            "",
            null,
            null,
            setOf(),
            setOf(),
            null,
            "dueDate",
            "DESC"
        )
        assertEquals(res.size, 3)
        assertEquals(items[1], res[0])
        assertEquals(items[2], res[1])
        assertEquals(items[0], res[2])

        res = getItems(
            boardId1.toString(),
            "",
            null,
            null,
            setOf(),
            setOf(),
            null,
            "label",
            "ASC"
        )
        assertEquals(res.size, 3)
        assertEquals(items[0], res[0])
        assertEquals(items[2], res[1])
        assertEquals(items[1], res[2])

        res = getItems(
            boardId1.toString(),
            "",
            null,
            null,
            setOf(),
            setOf(),
            null,
            "label",
            "DESC"
        )
        assertEquals(res.size, 3)
        assertEquals(items[1], res[0])
        assertEquals(items[0], res[1])
        assertEquals(items[2], res[2])

        res = getItems(
            boardId1.toString(),
            "",
            null,
            null,
            setOf(),
            setOf(),
            null,
            "priority",
            "ASC"
        )
        assertEquals(res.size, 3)
        assertEquals(items[0], res[0])
        assertEquals(items[2], res[1])
        assertEquals(items[1], res[2])

        res = getItems(
            boardId1.toString(),
            "",
            null,
            null,
            setOf(),
            setOf(),
            null,
            "priority",
            "DESC"
        )
        assertEquals(res.size, 3)
        assertEquals(items[1], res[0])
        assertEquals(items[0], res[1])
        assertEquals(items[2], res[2])

    }

    @Test
    fun searching() {
        val boardId1 = UUID.randomUUID()
        val items = listOf(
            Item(title = "item1", boardId = boardId1, owner = user),
            Item(title = "item2", boardId = boardId1, owner = user),
            Item(title = "item1", boardId = boardId1, owner = user)
        )
        items.forEach { ItemService.addItem(it) }

        val expectedResult = items.filter { it.title == "item1" }
        var searchResult = getItems(
            boardId1.toString(),
            "",
            null,
            null,
            setOf(),
            setOf(),
            "item1",
            "ordering",
            "ASC"
        )
        assertEquals(expectedResult, searchResult)

        searchResult = getItems(
            boardId1.toString(),
            "",
            null,
            null,
            setOf(),
            setOf(),
            "item",
            "ordering",
            "ASC"
        )
        assertEquals(items, searchResult)

    }

    @Test
    fun attachments() {
        // test add
        val boardId = UUID.randomUUID()
        val item = Item("1", boardId = boardId, owner = user)

        ItemService.addItem(item)

        ItemService.addAttachment(item.id, Attachment("attachment"))

        val res = ItemService.getItem(item.id)

        assertEquals(1, res.attachments.size)
        assertEquals("attachment", res.attachments.first().name)

        // Test delete
        ItemService.deleteAttachment(item.id, "attachment")
        assertFalse(File("data/${item.id}/attachment").exists())
    }
}
