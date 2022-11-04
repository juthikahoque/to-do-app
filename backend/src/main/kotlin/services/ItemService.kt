package services

import models.*
import java.sql.Connection
import java.sql.*
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*


object ItemService {


    private lateinit var conn: Connection

    fun init(connection: Connection) {
        conn = connection
        // create table if dne
        val statement = conn.createStatement()
        statement.executeUpdate("""
            CREATE TABLE IF NOT EXISTS items (
                id TEXT NOT NULL PRIMARY KEY,
                text VARCHAR(1000),
                dueDate TEXT,
                priority INT,
                done BOOLEAN,
                boardId TEXT,
                FOREIGN KEY(boardId) REFERENCES boards(id));
        """.trimIndent())

        statement.executeUpdate(
            """
            CREATE TABLE IF NOT EXISTS items_labels (
                itemId TEXT NOT NULL,
                label TEXT NOT NULL,
                PRIMARY KEY (itemId, label),
                FOREIGN KEY (itemId) REFERENCES items(id)
            )
        """.trimIndent()
        )

    }

    fun addItem(item: Item): Item {
        try {
            // prepared statements
            val insertItems = conn.prepareStatement("INSERT INTO items (id, text, dueDate, priority, done, boardId) VALUES (?, ?, ?, ?, ?, ?)")
            var insertLabels = conn.prepareStatement("INSERT INTO items_labels (itemId, label) VALUES (?, ?);")

            println(item)

            // insert into items table
            insertItems.setString(1, item.id.toString())
            insertItems.setString(2, item.text)
            insertItems.setString(3, item.dueDate.toString())
            insertItems.setInt(4, item.priority)
            insertItems.setBoolean(5, item.done)
            insertItems.setString(6, item.boardId.toString())

            insertItems.executeUpdate()

            // insert into items_labels table
            for (label in item.labels) {
                insertLabels.setString(1, item.id.toString())
                insertLabels.setString(2, label.value)
                insertLabels.executeUpdate()
            }
            return item
        } catch (ex: SQLException) {
            error(ex.message?:"sql add item failed")
        }
    }

    fun deleteItem(id: UUID): Boolean {
        try {
            // prepared statements
            val deleteItems = conn.prepareStatement("DELETE FROM items WHERE id = ?")
            var deleteLabels = conn.prepareStatement("DELETE FROM items_labels WHERE itemId = ?")

            // delete from items_labels table
            deleteLabels.setString(1, id.toString())
            deleteLabels.executeUpdate()

            // delete from items table
            deleteItems.setString(1, id.toString())
            val rowItems = deleteItems.executeUpdate()

            return rowItems != 0
        } catch (ex: SQLException) {
            error(ex.message?:"sql add item failed")
        }
    }

    fun getItemLabels(id: UUID) : MutableSet<Label> {
        try {
            val getLabelsWithId = conn.prepareStatement("SELECT * FROM items_labels WHERE itemId = ?")
            getLabelsWithId.setString(1, id.toString())

            val results = getLabelsWithId.executeQuery()
            val labels = mutableSetOf<Label>()
            while (results.next()) {
                val label = results.getString("label")
                labels.add(Label(label))
            }
            return labels
        } catch (ex: SQLException) {
            error("not found")
        }
    }

    fun getItemFromRes(res: ResultSet) : Item {
        val itemId = UUID.fromString(res.getString("id"))

        // get item labels
        val labels: MutableSet<Label> = getItemLabels(itemId)
        val dueDateStr = res.getString("dueDate")

        return Item(
            text = res.getString("text"),
            dueDate = if (dueDateStr == "null") null else LocalDateTime.parse(dueDateStr),
            boardId = UUID.fromString(res.getString("boardId")),
            labels = labels,
            priority = res.getInt("priority"),
            id = itemId,
            done = res.getBoolean("done")
        )
    }

    fun getItem(id: UUID): Item {
        try {
            val getItemWithId = conn.prepareStatement("SELECT * FROM items WHERE id = ?")
            getItemWithId.setString(1, id.toString())

            val res = getItemWithId.executeQuery()
            if(res.next()) {
                val item = getItemFromRes(res)
                res.close()
                return item
            } else {
                error("not found")
            }
        } catch (ex: SQLException) {
            error("not found")
        }
    }

    fun getAllItems(boardId: UUID) : List<Item> {
        try {
            val getItemWithId = conn.prepareStatement("SELECT * FROM items WHERE boardId = ?")
            getItemWithId.setString(1, boardId.toString())
            val res = getItemWithId.executeQuery()

            val itemsList = mutableListOf<Item>()
            while (res.next()) {
                itemsList.add(getItemFromRes(res))
            }
            res.close()
            return itemsList
        } catch (ex: SQLException) {
            error("not found")
        }
    }

    fun updateItem(new: Item): Item {
        try {
            val updateItem = conn.prepareStatement("UPDATE items SET text = ?, dueDate = ?, priority = ?, done = ? WHERE id = ?")
            updateItem.setString(1, new.text)
            updateItem.setString(2, new.dueDate.toString())
            updateItem.setInt(3, new.priority)
            updateItem.setBoolean(4, new.done)
            updateItem.setString(5, new.id.toString())

            updateItem.executeUpdate()

            val deleteLabels = conn.prepareStatement("DELETE FROM items_labels WHERE itemId = ?")
            deleteLabels.setString(1, new.id.toString())
            deleteLabels.executeUpdate()

            val addLabels = conn.prepareStatement("INSERT INTO items_labels (itemId, label) VALUES (?, ?);")
            for(label in new.labels) {
                addLabels.setString(1, new.id.toString())
                addLabels.setString(2, label.value)
                addLabels.executeUpdate()
            }

            return getItem(new.id)
        } catch (ex: SQLException) {
            error("${ex.message}")
        }
    }

    fun markItemAsDone(item: Item): Boolean {
        try {
            val updateItem = conn.prepareStatement("UPDATE items SET done = ? WHERE id = ?")
            val done = true
            updateItem.setBoolean(1, done)
            updateItem.setString(2, item.id.toString())
            val updated = updateItem.executeUpdate()
            return updated != 0
        } catch (ex: SQLException) {
            error("not updated")
        }
    }

    fun filterByDate(startDateTime: LocalDateTime, boardId: UUID, endDateTime: LocalDateTime? = null): List<Item> {
        try {
            val startDate = startDateTime.toLocalDate()
            val endDate = if(endDateTime != null) endDateTime.toLocalDate() else startDate.plusDays(1)

            val getItems = conn.prepareStatement("SELECT * FROM items where dueDate >= ? AND dueDate < ? and boardId = ?")

            getItems.setString(1, startDate.toString())
            getItems.setString(2, endDate.toString())
            getItems.setString(3, boardId.toString())

            val res = getItems.executeQuery()

            val itemsList = mutableListOf<Item>()
            while (res.next()) {
                itemsList.add(getItemFromRes(res))
            }
            res.close()
            return itemsList
        } catch (ex: SQLException) {
            error("not updated")
        }
    }

    fun filterByLabel(labels: MutableSet<Label>, boardId: UUID) : List<Item> {
        try {
            val itemsList = mutableListOf<Item>()
            for(label in labels) {
                val labelText = label.value
                val getItems = conn.prepareStatement("SELECT * FROM items INNER JOIN items_labels ON items.id = items_labels.itemId WHERE label = ? and items.boardId = ?")
                getItems.setString(1, labelText)
                getItems.setString(2, boardId.toString())

                val res = getItems.executeQuery()

                while (res.next()) {
                    val curItem = getItemFromRes(res)
                    if(!itemsList.contains(curItem)) {
                        itemsList.add(curItem)
                    }
                }
                res.close()
            }
            return itemsList
        } catch (ex: SQLException) {
            error("not updated")
        }
    }

    fun filterByPriority(priorities: MutableSet<Int>, boardId: UUID): List<Item> {
        try {
            val itemsList = mutableListOf<Item>()
            for(priority in priorities) {
                val getItems = conn.prepareStatement("SELECT * FROM items WHERE priority = ? and boardId = ?")
                getItems.setInt(1, priority)
                getItems.setString(2, boardId.toString())

                val res = getItems.executeQuery()

                while (res.next()) {
                    val curItem = getItemFromRes(res)
                    if(!itemsList.contains(curItem)) {
                        itemsList.add(curItem)
                    }
                }
                res.close()
            }
            return itemsList
        } catch (ex: SQLException) {
            error("not updated")
        }
    }

    fun sortByPriority(boardId: UUID, order: String): List<Item> {
        try {
            val itemsList = mutableListOf<Item>()
            var getItems : PreparedStatement
            if(order == "ASC") {
                getItems = conn.prepareStatement("SELECT * FROM items WHERE boardId = ? ORDER BY priority ASC")
            } else {
                getItems = conn.prepareStatement("SELECT * FROM items WHERE boardId = ? ORDER BY priority DESC")
            }
            getItems.setString(1, boardId.toString())
            val res = getItems.executeQuery()

            while (res.next()) {
                val curItem = getItemFromRes(res)
                if(!itemsList.contains(curItem)) {
                    itemsList.add(curItem)
                }
            }
            res.close()
            return itemsList
        } catch (ex: SQLException) {
            error("not updated")
        }
    }
    fun sortByDueDates(boardId: UUID, order: String): List<Item> {
        try {
            val itemsList = mutableListOf<Item>()

            var getItems : PreparedStatement
            if(order == "ASC") {
                getItems = conn.prepareStatement("SELECT * FROM items WHERE boardId = ? ORDER BY dueDate ASC")
                getItems.setString(1, boardId.toString())
            } else {
                getItems = conn.prepareStatement("SELECT * FROM items WHERE boardId = ? ORDER BY dueDate DESC")
                getItems.setString(1, boardId.toString())
            }
            val res = getItems.executeQuery()

            while (res.next()) {
                val curItem = getItemFromRes(res)
                if(!itemsList.contains(curItem)) {
                    itemsList.add(curItem)
                }
            }
            res.close()
            return itemsList
        } catch (ex: SQLException) {
            error("not updated")
        }
    }

    fun sortByLabels(boardId: UUID, order: String): List<Item> {
        try {
            val itemsList = mutableListOf<Item>()

            var getItems : PreparedStatement
            if(order == "ASC") {
                getItems = conn.prepareStatement("SELECT * FROM items INNER JOIN items_labels ON items.id = items_labels.itemId WHERE items.boardId = ? ORDER BY label ASC")
            } else {
                getItems = conn.prepareStatement("SELECT * FROM items INNER JOIN items_labels ON items.id = items_labels.itemId WHERE items.boardId = ? ORDER BY label DESC")
            }
            getItems.setString(1, boardId.toString())
            val res = getItems.executeQuery()

            while (res.next()) {
                val curItem = getItemFromRes(res)
                if(!itemsList.contains(curItem)) {
                    itemsList.add(curItem)
                }
            }
            res.close()
            return itemsList
        } catch (ex: SQLException) {
            error("not updated")
        }
    }

}