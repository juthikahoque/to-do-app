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

    fun updateItem(id: UUID, updatedItem: Item) {
        items.find { it.id == id }?.boardId = updatedItem.boardId
        items.find { it.id == id }?.dueDate = updatedItem.dueDate
        items.find { it.id == id }?.text = updatedItem.text
        items.find { it.id == id }?.priority = updatedItem.priority
        items.find { it.id == id }?.labels = updatedItem.labels
    }

}