package services

import kotlinx.serialization.Serializable
import models.*
import models.DateTimeSerializer
import models.UUIDSerializer
import java.sql.Connection
import java.sql.*
import java.sql.PreparedStatement
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
                id INT NOT NULL PRIMARY KEY,
                text VARCHAR(1000),
                dueDate DATETIME,
                priority INT,
                done BOOLEAN,
                boardId INT,
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
            val insertItems: PreparedStatement = conn.prepareStatement("INSERT INTO items (id, text, dueDate, priority, done, boardId) VALUES (?, ?, ?, ?, ?, ?)")
            var insertLabels: PreparedStatement = conn.prepareStatement("INSERT INTO items_labels (itemId, label) VALUES (?, ?);")

            // insert into items table
            insertItems.setString(1, item.id.toString())
            insertItems.setString(2, item.text)
            insertItems.setString(3, item.dueDate.toString())
            insertItems.setString(4, item.priority.toString())
            insertItems.setString(5, item.done.toString())
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
            error("sql error")
        }
    }

    fun deleteItem(id: UUID): Boolean {
        try {
            // prepared statements
            val deleteItems: PreparedStatement = conn.prepareStatement("DELETE FROM items WHERE id = ?")
            var deleteLabels: PreparedStatement = conn.prepareStatement("DELETE FROM items_labels WHERE itemId = ?")

            // delete from items_labels table
            deleteLabels.setString(1, id.toString())
            val rowLabels = deleteLabels.executeUpdate()

            // delete from items table
            deleteItems.setString(1, id.toString())
            val rowItems = deleteItems.executeUpdate()

            return rowItems != 0
        } catch (ex: SQLException) {
            error("sql error")
        }
    }

    fun getItemLabels(id: UUID) : MutableSet<Label> {
        try {
            val getLabelsWithId: PreparedStatement = conn.prepareStatement("SELECT * FROM items_labels WHERE itemId = ?")
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

        return Item(
            text = res.getString("text"),
            dueDate = LocalDateTime.parse(res.getString("dueDate")),
            boardId = UUID.fromString(res.getString("boardId")),
            labels = labels,
            priority = res.getInt("priority"),
            id = itemId,
            done = res.getBoolean("done")
        )
    }

    fun getItem(id: UUID): Item {
        try {
            val getItemWithId: PreparedStatement = conn.prepareStatement("SELECT * FROM items WHERE id = ?")
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
            val getItemWithId: PreparedStatement = conn.prepareStatement("SELECT * FROM items WHERE boardId = ?")
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
            updateItem.setString(3, new.priority.toString())
            updateItem.setString(4, new.done.toString())
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
            updateItem.setString(1, done.toString())
            updateItem.setString(2, item.id.toString())
            val updated = updateItem.executeUpdate()
            return updated != 0
        } catch (ex: SQLException) {
            error("not updated")
        }
    }
}