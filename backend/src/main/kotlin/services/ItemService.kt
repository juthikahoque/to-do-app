package services

import models.Item
import java.util.UUID


class ItemService {

    var items: MutableSet<Item> = mutableSetOf()

    fun addItem(item: Item) {
        items.add(item)
    }

    fun deleteItem(id: UUID) {
        items.removeIf { it.id == id }
    }

    fun getItem(id: UUID): Item {
        return items.first { it.id == id }
    }

    fun getAllItems(boardId: UUID) : List<Item> {
        return items.filter { it.boardId == boardId }
    }

    fun updateItem(new: Item): Item? {
        val item = items.find { it.id == new.id } ?: return null
        items.remove(item)
        items.add(new)
        return new
    }
}