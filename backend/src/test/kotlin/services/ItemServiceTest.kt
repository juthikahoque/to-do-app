package services

import models.Item
import models.Label
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class ItemServiceTest {
    @Test
    fun addItem() {
        val itemsLen = ItemService.items.size
        val item = Item("todo")

        ItemService.addItem(item)

        assertTrue(ItemService.items.contains(item))
        assertEquals(itemsLen + 1, ItemService.items.size)
    }

    @Test
    fun getAllItems() {
        val firstItem: Item? = if (ItemService.items.size > 0) ItemService.items.elementAt(0) else null

        lateinit var boardId: UUID
        if(firstItem != null) {
            boardId = firstItem.boardId
        } else {
            val newItem = Item("new item")
            boardId = newItem.boardId
            ItemService.addItem(newItem)
        }

        val itemsWithBoardId = ItemService.getAllItems(boardId)

        var currentItemsWithBoardId: MutableSet<Item> = mutableSetOf()
        for (it in ItemService.items) {
            if (it.boardId == boardId) {
                currentItemsWithBoardId.add(it)
            }
        }

        assertEquals(currentItemsWithBoardId.size, itemsWithBoardId.size)
    }

    @Test
    fun getBoard() {
        val newItem = Item("item")
        val itemId = newItem.id
        ItemService.addItem(newItem)

        val itemWithId = ItemService.getItem(itemId)

        assertEquals(itemWithId, newItem)
    }

    @Test
    fun updateBoard() {
        val boardId = UUID.randomUUID()
        val newItem = Item("board", LocalDateTime.now(), boardId, mutableSetOf(Label("label")), 1, UUID.randomUUID(), true)
        val updatedItem = Item("updated", LocalDateTime.now(), boardId, mutableSetOf(Label("updated")), 2, newItem.id, false)
        ItemService.addItem(newItem)

        val itemAfterUpdate = ItemService.updateItem(updatedItem)!! // throws if not null

        assertEquals(boardId, itemAfterUpdate.boardId)
        assertEquals(newItem.id, itemAfterUpdate.id)
        assertFalse(itemAfterUpdate.done)
        assertEquals(updatedItem.priority, itemAfterUpdate.priority)
        assertEquals("updated", itemAfterUpdate.text)
        assertEquals(Label("updated"), itemAfterUpdate.labels.first())
    }

    @Test
    fun deleteBoard() {
        val newItem = Item("item")
        val itemId = newItem.id
        ItemService.addItem(newItem)

        val isDeletedTrue = ItemService.deleteItem(itemId)

        assertTrue(isDeletedTrue)
        assertFalse(ItemService.items.contains(newItem))

        val isDeletedFalse = ItemService.deleteItem(itemId)

        assertFalse(isDeletedFalse)
    }

    @Test
    fun testDone() {
        val newItem = Item("item")
        ItemService.addItem(newItem)

        ItemService.markItemAsDone(newItem);

        val updatedItem = ItemService.items.first { it.id == newItem.id }
        assertTrue(updatedItem.done)
    }
}

