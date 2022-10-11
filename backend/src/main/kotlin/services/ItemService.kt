package services

import models.Item
import java.util.UUID


class ItemService {

    var items: MutableSet<Item> = mutableSetOf()

    fun createItem(item: Item) {
        items.add(item)
    }

    fun deleteItem(id: UUID) {
        items.removeIf { it.id == id }
    }

    fun getItem(id: UUID): Item? {
        return items.find { it.id == id }
    }

    fun getAllItems(boardId: UUID) : List<Item> {
        return items.filter { it.boardId == boardId }
    }

    fun updateItem(id: UUID, updatedItem: Item) {
        val item = items.find { it.id == id }
        if (item == null) return
        
        item.boardId = updatedItem.boardId
        item.dueDate = updatedItem.dueDate
        item.text = updatedItem.text
        item.priority = updatedItem.priority
        item.labels = updatedItem.labels
    }
}