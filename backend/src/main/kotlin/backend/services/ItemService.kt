package backend.services

import models.*
import java.io.File
import java.io.InputStream
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
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
                id TEXT NOT NULL PRIMARY KEY,
                text VARCHAR(1000),
                dueDate TEXT,
                priority INT,
                done BOOLEAN,
                boardId TEXT,
                owner TEXT,
                description TEXT,
                ordering INT NOT NULL,
                FOREIGN KEY(boardId) REFERENCES boards(id));
        """.trimIndent()
        )

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

        statement.executeUpdate(
            """
            CREATE TABLE IF NOT EXISTS items_attachments (
                itemId TEXT NOT NULL,
                path TEXT NOT NULL,
                PRIMARY KEY (itemId, path),
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
                INSERT INTO items (id, text, dueDate, priority, done, boardId, owner, description, ordering) 
                SELECT ?, ?, ?, ?, ?, ?, ?, ?, COALESCE(MAX(ordering) + 1, 0) FROM items WHERE boardId = ?
                """.trimIndent()
            )
            val insertLabels = conn.prepareStatement("INSERT INTO items_labels (itemId, label) VALUES (?, ?)")

            var idx = 0
            // insert into items table
            insertItems.setString(++idx, item.id.toString())
            insertItems.setString(++idx, item.title)
            insertItems.setString(++idx, item.dueDate.toString())
            insertItems.setInt(++idx, item.priority)
            insertItems.setBoolean(++idx, item.done)
            insertItems.setString(++idx, item.boardId.toString())
            insertItems.setString(++idx, item.owner.userId)
            insertItems.setString(++idx, item.description)
            insertItems.setString(++idx, item.boardId.toString())

            insertItems.executeUpdate()

            // insert into items_labels table
            for (label in item.labels) {
                insertLabels.setString(1, item.id.toString())
                insertLabels.setString(2, label.value)
                insertLabels.executeUpdate()
            }
            return item
        } catch (ex: SQLException) {
            appError(ex.message ?: "sql add item failed")
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

            // delete attachments
            deleteAttachment(id, "%")

            // delete from items table
            deleteItems.setString(1, id.toString())
            val rowItems = deleteItems.executeUpdate()

            return rowItems != 0
        } catch (ex: SQLException) {
            appError(ex.message ?: "sql delete item failed")
        }
    }

    private fun getItemLabels(id: UUID): MutableSet<Label> {
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
            appError(ex.message ?: "sql get item labels failed")
        }
    }

    private val getItemAttachment by lazy {
        conn.prepareStatement("""SELECT * FROM items_attachments WHERE itemId = ?""")
    }

    private fun getItemAttachments(id: UUID): MutableSet<Attachment> {
        try {
            getItemAttachment.setString(1, id.toString())
            val results = getItemAttachment.executeQuery()
            val attachments = mutableSetOf<Attachment>()
            while (results.next()) {
                val path = results.getString("path")
                attachments.add(Attachment(path))
            }
            return attachments
        } catch (ex: SQLException) {
            appError(ex.message ?: "sql get attachments failed")
        }
    }

    private fun getItemFromRes(res: ResultSet): Item {
        val itemId = UUID.fromString(res.getString("id"))

        // get item labels
        val labels = getItemLabels(itemId)
        val attachments = getItemAttachments(itemId)

        val dueDateStr = res.getString("dueDate")

        return Item(
            title = res.getString("text"),
            dueDate = if (dueDateStr == "null") null else LocalDateTime.parse(dueDateStr),
            boardId = UUID.fromString(res.getString("boardId")),
            labels = labels,
            priority = res.getInt("priority"),
            id = itemId,
            done = res.getBoolean("done"),
            attachments = attachments,
            owner = UserService.getUserById(res.getString("owner")),
            description = res.getString("description"),
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
                appError("item with id $id was not found", AppError.NotFound)
            }
        } catch (ex: SQLException) {
            appError(ex.message ?: "sql get item by id failed")
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
            appError(ex.message ?: "sql get all item failed")
        }
    }

    fun updateItem(new: Item): Item {
        try {
            val updateItem = conn.prepareStatement(
                """UPDATE items SET text = ?, dueDate = ?, priority = ?, done = ?, owner = ?, description = ? WHERE id = ?"""
            )
            var idx = 0

            updateItem.setString(++idx, new.title)
            updateItem.setString(++idx, new.dueDate.toString())
            updateItem.setInt(++idx, new.priority)
            updateItem.setBoolean(++idx, new.done)
            updateItem.setString(++idx, new.owner.userId)
            updateItem.setString(++idx, new.description)
            updateItem.setString(++idx, new.id.toString())

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
            appError(ex.message ?: "sql update item failed")
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
            appError(ex.message ?: "sql mark item as done failed")
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
            appError(ex.message ?: "sql change order failed")
        }
    }

    private val addAttachment by lazy {
        conn.prepareStatement("""INSERT INTO items_attachments (itemId, path) VALUES (?, ?)""")
    }

    fun addAttachment(itemId: UUID, name: String, data: InputStream) {
        try {
            // save file
            val file = File("data/$itemId/$name")
            file.parentFile.mkdirs()
            data.use { input ->
                // copy the stream to the file with buffering
                file.outputStream().buffered().use {
                    // note that this is blocking
                    input.copyTo(it)
                }
            }

            // add to db
            addAttachment.setString(1, itemId.toString())
            addAttachment.setString(2, name)

            addAttachment.executeUpdate()
        } catch (ex: SQLException) {
            appError(ex.message ?: "sql add attachments failed")
        }
    }

    private val deleteAttachment by lazy {
        conn.prepareStatement("""DELETE FROM items_attachments WHERE itemId = ? AND path LIKE ?""")
    }

    fun deleteAttachment(itemId: UUID, name: String) {
        try {
            if (name == "%") { // delete all
                File("data/$itemId").deleteRecursively()
            } else {
                File("data/$itemId/$name").delete()
            }

            deleteAttachment.setString(1, itemId.toString())
            deleteAttachment.setString(2, name)

            deleteAttachment.executeUpdate() // == 0 if no item exist
        } catch (ex: SQLException) {
            appError(ex.message ?: "sql delete attachment failed")
        }
    }

    private fun getItemQuery(allBoard: Boolean, query: String, sort: String): PreparedStatement {
        return if (allBoard)
            conn.prepareStatement(
                """
                SELECT * FROM items 
                INNER JOIN boards_users ON boards_users.boardId = items.boardId AND userId = ?
                LEFT JOIN items_labels ON items_labels.itemId = items.id WHERE true
                $query
                GROUP BY id ORDER BY $sort
            """.trimIndent()
            )
        else
            conn.prepareStatement(
                """
                SELECT * FROM items
                LEFT JOIN items_labels ON items_labels.itemId = items.id WHERE boardId = ?
                $query
                GROUP BY id ORDER BY $sort
            """.trimIndent()
            )
    }

    fun getItems(
        boardId: String,
        userId: String,
        startDateTime: LocalDateTime?,
        endDateTime: LocalDateTime?,
        labels: MutableSet<Label>,
        priorities: MutableSet<Int>,
        search: String?,
        actualSortBy: String?,
        actualOrderBy: String?,
    ): MutableList<Item> {
        try {
            // check sort + order errors
            val sortBy = actualSortBy ?: "ordering"
            val order = actualOrderBy ?: "ASC"
            if (sortBy != "dueDate" && sortBy != "priority" && sortBy != "label" && sortBy != "ordering") {
                appError("invalid sortBy entry, enter one of: dueDate, priority, label, ordering", AppError.BadRequest)
            }
            if (order != "ASC" && order != "DESC") {
                appError("invalid order entry, enter one of: ASC, DESC", AppError.BadRequest)
            }

            var query = ""
            if (startDateTime != null) {
                query += " AND (dueDate >= ? AND dueDate < ?)"
            }
            if (labels.size > 0) {
                query += " AND (label = ?${" OR label = ?".repeat(labels.size - 1)})"
            }
            if (priorities.size > 0) {
                query += " AND (priority = ?${" OR priority = ?".repeat(priorities.size - 1)})"
            }
            if (search != null) {
                query += " AND (text LIKE ?)"
            }

            val sortStr = if (sortBy != "ordering") {
                "$sortBy $order, ordering"
            } else {
                "$sortBy $order"
            }

            val statement = getItemQuery(boardId.isEmpty(), query, sortStr)
            var sqlIndex = 0
            statement.setString(++sqlIndex, boardId.ifEmpty { userId })

            // substitute values
            if (startDateTime != null) {
                val endDate = endDateTime ?: startDateTime.plusDays(1)
                statement.setString(++sqlIndex, startDateTime.toString())
                statement.setString(++sqlIndex, endDate.toString())
            }
            for (label in labels) {
                statement.setString(++sqlIndex, label.value)
            }
            for (priority in priorities) {
                statement.setInt(++sqlIndex, priority)
            }
            if (search != null) {
                statement.setString(++sqlIndex, "%$search%")
            }

            // get results
            val res = statement.executeQuery()
            val itemsList = mutableListOf<Item>()
            while (res.next()) {
                itemsList.add(getItemFromRes(res))
            }
            res.close()
            return itemsList
        } catch (ex: SQLException) {
            appError(
                """
                get items failed with input:
                    startDateTime = $startDateTime
                    endDateTime = $endDateTime
                    labels = $labels
                    priorities = $priorities
                    search = $search
                    actualSortBy = $actualSortBy
                    actualOrderBy = $actualOrderBy
                error: ${ex.message}
            """.trimIndent(), AppError.Unexpected
            )
        }

    }

}