package services

import models.Item
import java.sql.Connection
import java.util.*


object ItemService {

    var items: MutableSet<Item> = mutableSetOf()

    fun init(conn: Connection) {

    }

    fun addItem(item: Item) {
        items.add(item)
    }

    fun deleteItem(id: UUID): Boolean {
        return items.removeIf { it.id == id }
    }

    fun getItem(id: UUID): Item {
        return items.first { it.id == id }
    }

    fun getAllItems(boardId: UUID) : List<Item> {
        return items.filter { it.boardId == boardId }
    }

    fun getAllItems(): List<Item> {
        return items.toList()
    }

    fun updateItem(new: Item): Item? {
        val item = items.find { it.id == new.id } ?: return null
        items.remove(item)
        items.add(new)
        return new
    }

    fun markItemAsDone(item: Item) {
        updateItem(item.copy(done = true))
    }
}