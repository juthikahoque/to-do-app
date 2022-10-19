package services

import models.Item
import models.Label
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class ItemServiceTest {
    @Test
    fun addItem() {
        val itemService = ItemService()
        val itemsLen = itemService.items.size
        val item = Item("todo")

        itemService.addItem(item)

        assertTrue(itemService.items.contains(item))
        assertEquals(itemsLen + 1, itemService.items.size)
    }

    @Test
    fun getAllItems() {
        val itemService = ItemService()
        val firstItem: Item? = if(itemService.items.size > 0) itemService.items.elementAt(0) else null

        lateinit var boardId: UUID
        if(firstItem != null) {
            boardId = firstItem.boardId
        } else {
            val newItem = Item("new item")
            boardId = newItem.boardId
            itemService.addItem(newItem)
        }

        val itemsWithBoardId = itemService.getAllItems(boardId)

        var currentItemsWithBoardId: MutableSet<Item> = mutableSetOf()
        for (it in itemService.items) {
            if(it.boardId == boardId) {
                currentItemsWithBoardId.add(it)
            }
        }

        assertEquals(currentItemsWithBoardId.size, itemsWithBoardId.size)
    }

    @Test
    fun getBoard() {
        val itemService = ItemService()
        val newItem = Item("item")
        val itemId = newItem.id
        itemService.addItem(newItem)

        val itemWithId = itemService.getItem(itemId)

        assertEquals(itemWithId, newItem)
    }

    @Test
    fun updateBoard() {
        val boardId = UUID.randomUUID()
        val itemService = ItemService()
        val newItem = Item("board", LocalDateTime.now(), boardId, mutableSetOf(Label("label")), 1, UUID.randomUUID(), true)
        val updatedItem = Item("updated", LocalDateTime.now(), boardId, mutableSetOf(Label("updated")), 2, newItem.id, false)
        itemService.addItem(newItem)

        val itemAfterUpdate = itemService.updateItem(updatedItem)!! // throws if not null

        assertEquals(boardId, itemAfterUpdate.boardId)
        assertEquals(newItem.id, itemAfterUpdate.id)
        assertFalse(itemAfterUpdate.done)
        assertEquals(updatedItem.priority, itemAfterUpdate.priority)
        assertEquals("updated", itemAfterUpdate.text)
        assertEquals(Label("updated"), itemAfterUpdate.labels.first())
    }

    @Test
    fun deleteBoard() {
        val itemService = ItemService()
        val newItem = Item("item")
        val itemId = newItem.id
        itemService.addItem(newItem)

        val isDeletedTrue = itemService.deleteItem(itemId)

        assertTrue(isDeletedTrue)
        assertFalse(itemService.items.contains(newItem))

        val isDeletedFalse = itemService.deleteItem(itemId)

        assertFalse(isDeletedFalse)
    }

    @Test
    fun testDone() {
        val itemService = ItemService()
        val newItem = Item("item")
        itemService.addItem(newItem)

        var itemsNotDone = itemService.items.filter { !it.done }

        assertEquals(itemsNotDone.size, itemService.items.size)

        itemService.markItemAsDone(newItem);

        assertTrue(newItem.done)
    }
}

