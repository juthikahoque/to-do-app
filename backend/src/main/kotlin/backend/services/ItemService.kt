package backend.services

import models.*
import java.sql.Connection
import java.sql.*
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*
import kotlin.math.max
import kotlin.math.min


object ItemService {


    private lateinit var conn: Connection

    fun init(connection: Connection) {
        conn = connection
        // create table if dne
        val statement = conn.createStatement()
        statement.executeUpdate(
            """
            CREATE TABLE IF NOT EXISTS items (
                id INT NOT NULL PRIMARY KEY,
                text VARCHAR(1000),
                dueDate DATETIME,
                priority INT,
                done BOOLEAN,
                boardId INT,
                ordering INT NOT NULL UNIQUE,
                FOREIGN KEY(boardId) REFERENCES boards(id))
        """.trimIndent()
        )

        statement.executeUpdate(
            """
            CREATE TABLE IF NOT EXISTS items_labels (
                itemId INT NOT NULL,
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
            val insertItems = conn.prepareStatement(
                """
                INSERT INTO items (id, text, dueDate, priority, done, boardId, ordering) 
                SELECT ?, ?, ?, ?, ?, ?, COALESCE(MAX(ordering) + 1, 0) FROM items WHERE boardId = ?
                """.trimIndent()
            )
            val insertLabels = conn.prepareStatement("INSERT INTO items_labels (itemId, label) VALUES (?, ?)")

            // insert into items table
            insertItems.setString(1, item.id.toString())
            insertItems.setString(2, item.text)
            insertItems.setString(3, item.dueDate.toString())
            insertItems.setInt(4, item.priority)
            insertItems.setBoolean(5, item.done)
            insertItems.setString(6, item.boardId.toString())
            insertItems.setString(7, item.boardId.toString())

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
            val deleteLabels = conn.prepareStatement("DELETE FROM items_labels WHERE itemId = ?")

            // delete from items_labels table
            deleteLabels.setString(1, id.toString())
            deleteLabels.executeUpdate()

            // delete from items table
            deleteItems.setString(1, id.toString())
            val rowItems = deleteItems.executeUpdate()

            return rowItems != 0
        } catch (ex: SQLException) {
            error(ex.message?:"sql delete item failed")
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
            error(ex.message?:"sql get item labels failed")
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
            if (res.next()) {
                val item = getItemFromRes(res)
                res.close()
                return item
            } else {
                error("item with id $id was not found")
            }
        } catch (ex: SQLException) {
            error(ex.message?:"sql get item by id failed")
        }
    }

    fun getAllItems(boardId: UUID): List<Item> {
        try {
            val getItemWithId = conn.prepareStatement("SELECT * FROM items WHERE boardId = ? ORDER BY ordering")
            getItemWithId.setString(1, boardId.toString())
            val res = getItemWithId.executeQuery()

            val itemsList = mutableListOf<Item>()
            while (res.next()) {
                itemsList.add(getItemFromRes(res))
            }
            res.close()
            return itemsList
        } catch (ex: SQLException) {
            error(ex.message?:"sql get all item failed")
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

            val addLabels = conn.prepareStatement("INSERT INTO items_labels (itemId, label) VALUES (?, ?)")
            for (label in new.labels) {
                addLabels.setString(1, new.id.toString())
                addLabels.setString(2, label.value)
                addLabels.executeUpdate()
            }

            return getItem(new.id)
        } catch (ex: SQLException) {
            error(ex.message?:"sql update item failed")
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
            error(ex.message?:"sql mark item as done failed")
        }
    }

    private val changeOrderSQL by lazy {
        listOf(
            conn.prepareStatement("UPDATE items SET ordering = -1 WHERE boardId = ? AND ordering = ?"),
            conn.prepareStatement("UPDATE items SET ordering = ordering + ? WHERE boardId = ? AND ordering BETWEEN ? AND ?"),
            conn.prepareStatement("UPDATE items SET ordering = ? WHERE boardId = ? AND ordering = -1"),
        )
    }

    fun changeOrder(boardId: UUID, from: Int, to: Int) {
        try {
            val boardIdStr = boardId.toString()
            changeOrderSQL[0].setString(1, boardIdStr)
            changeOrderSQL[1].setString(2, boardIdStr)
            changeOrderSQL[2].setString(2, boardIdStr)

            changeOrderSQL[0].setInt(2, from)
            changeOrderSQL[1].setInt(1, if (from < to) -1 else 1)
            changeOrderSQL[1].setInt(3, min(from, to))
            changeOrderSQL[1].setInt(4, max(to, from))
            changeOrderSQL[2].setInt(1, to)

            changeOrderSQL.forEach { it.executeUpdate() }
        } catch (ex: SQLException) {
            error(ex.message?:"sql delete item failed")
        }
    }
}