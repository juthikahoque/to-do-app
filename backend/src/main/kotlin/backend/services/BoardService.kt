package backend.services

import models.Board
import models.Label
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*

object BoardService {
    private lateinit var conn: Connection

    private lateinit var getStatementSQL: String
    private lateinit var getStatement: List<PreparedStatement>
    private lateinit var addStatement: List<PreparedStatement>
    private lateinit var updateStatement: List<PreparedStatement>
    private lateinit var deleteStatement: List<PreparedStatement>

    fun init(connection: Connection) {
        conn = connection
        // create table if dne
        val statement = conn.createStatement()
        statement.executeUpdate(
            """
            CREATE TABLE IF NOT EXISTS boards (
                id TEXT NOT NULL,
                name TEXT,
                updated_at TEXT,
                created_at TEXT,
                PRIMARY KEY (id)
            )
        """.trimIndent()
        )
        statement.executeUpdate(
            """
            CREATE TABLE IF NOT EXISTS boards_users (
                boardId TEXT NOT NULL,
                userId TEXT NOT NULL,
                PRIMARY KEY (boardId, userId),
                FOREIGN KEY (boardId) REFERENCES boards(id)
            )
        """.trimIndent()
        )
        statement.executeUpdate(
            """
            CREATE TABLE IF NOT EXISTS boards_labels (
                boardId TEXT NOT NULL,
                label TEXT NOT NULL,
                PRIMARY KEY (boardId, label),
                FOREIGN KEY (boardId) REFERENCES boards(id)
            )
        """.trimIndent()
        )

        getStatementSQL = "SELECT * FROM boards INNER JOIN boards_users ON id = boardId"
        getStatement = listOf(
            conn.prepareStatement("$getStatementSQL AND userId = ?"),
            conn.prepareStatement("SELECT * FROM boards_users WHERE boardId = ?"),
            conn.prepareStatement("SELECT * FROM boards_labels WHERE boardId = ?"),
        )
        addStatement = listOf(
            conn.prepareStatement("INSERT INTO boards(id, name, updated_at, created_at) VALUES (?, ?, ?, ?)"),
            conn.prepareStatement("INSERT INTO boards_users(boardId, userId) VALUES (?, ?)"),
            conn.prepareStatement("INSERT INTO boards_labels(boardId, label) VALUES (?, ?)"),
        )
        updateStatement = listOf(
            conn.prepareStatement("UPDATE boards SET name = ?, updated_at = ? WHERE id = ?"),
        )
        deleteStatement = listOf(
            conn.prepareStatement("DELETE FROM boards WHERE id = ?"),
            conn.prepareStatement("DELETE FROM boards_users WHERE boardId = ?"),
            conn.prepareStatement("DELETE FROM boards_labels WHERE boardId = ?"),
        )
    }

    /**
     * create a board from a result set, add userId and Labels
     */
    private fun getBoardFromRes(res: ResultSet): Board {
        val boardId = res.getString("id")
        // get board users
        getStatement[1].setString(1, boardId)
        val usersRes = getStatement[1].executeQuery()
        val users = mutableSetOf<String>()
        while (usersRes.next()) {
            users.add(usersRes.getString("userId"))
        }
        // get board labels
        getStatement[2].setString(1, boardId)
        val labelsRes = getStatement[2].executeQuery()
        val labels = mutableSetOf<Label>()
        while (labelsRes.next()) {
            labels.add(Label(labelsRes.getString("label")))
        }
        return Board(
            name = res.getString("name"),
            users = users,
            id = UUID.fromString(boardId),
            labels = labels,
            updated_at = LocalDateTime.parse(res.getString("updated_at")),
            created_at = LocalDateTime.parse(res.getString("created_at")),
        )
    }

    fun addBoard(board: Board): Board {
        try {
            // insert into boards
            addStatement[0].setString(1, board.id.toString())
            addStatement[0].setString(2, board.name)
            addStatement[0].setString(3, board.updated_at.toString())
            addStatement[0].setString(4, board.created_at.toString())
            addStatement[0].executeUpdate()
            // insert into boards_users
            addStatement[1].setString(1, board.id.toString())
            for (userId in board.users) {
                addStatement[1].setString(2, userId.toString())
                addStatement[1].executeUpdate()
            }
            // insert into boards_labels
            addStatement[2].setString(1, board.id.toString())
            for (label in board.labels) {
                addStatement[2].setString(2, label.value)
                addStatement[2].executeUpdate()
            }
            // conn.commit() // if using transactions
            return board
        } catch (ex: SQLException) {
            print(ex)
            error("sql error")
        }
    }

    fun getBoards(userId: String): List<Board> {
        try {
            getStatement[0].setString(1, userId)
            val res = getStatement[0].executeQuery()
            val list = mutableListOf<Board>()
            while (res.next()) {
                list.add(getBoardFromRes(res))
            }
            res.close()
            return list
        } catch (ex: SQLException) {
            print(ex)
            error("not found")
        }
    }

    fun getBoard(id: UUID): Board {
        try {
            val stmt = conn.prepareStatement("$getStatementSQL AND id = ?")
            stmt.setString(1, id.toString())

            val res = stmt.executeQuery()

            if (res.next()) {
                val board = getBoardFromRes(res)
                res.close()
                return board
            } else {
                error("not found")
            }
        } catch (ex: SQLException) {
            print(ex)
            error("not found")
        }
    }

    fun updateBoard(new: Board): Board? {
        return try {
            // update boards
            updateStatement[0].setString(1, new.name)
            updateStatement[0].setString(2, LocalDateTime.now().toString())
            updateStatement[0].executeUpdate()
            // update boards_users
            getStatement[1].setString(1, new.id.toString())
            deleteStatement[1].setString(1, new.id.toString())
            addStatement[1].setString(1, new.id.toString())
            val userRes = getStatement[1].executeQuery()
            val users = new.users
            while (userRes.next()) {
                val userId = userRes.getString("userId")
                if (!users.remove(userId)) {
                    deleteStatement[1].setString(2, userId)
                    deleteStatement[1].executeUpdate()
                }
            }
            for (unAddedUsers in users) {
                addStatement[1].setString(2, unAddedUsers.toString())
                addStatement[1].executeUpdate()
            }
            // update boards_labels
            getStatement[2].setString(1, new.id.toString())
            deleteStatement[2].setString(1, new.id.toString())
            addStatement[2].setString(1, new.id.toString())
            val labelRes = getStatement[2].executeQuery()
            val labels = new.labels
            while (labelRes.next()) {
                val label = labelRes.getString("userId")
                if (!labels.remove(Label(label))) {
                    deleteStatement[1].setString(2, label)
                    deleteStatement[1].executeUpdate()
                }
            }
            for (unAddedLabel in labels) {
                addStatement[1].setString(2, unAddedLabel.value)
                addStatement[1].executeUpdate()
            }
            // conn.commit() // if using transactions
            new
        } catch (ex: SQLException) {
            print(ex.message)
            null
        }
    }

    fun deleteBoard(id: UUID): Boolean {
        return try {
            // delete from boards_labels
            deleteStatement[2].setString(1, id.toString())
            deleteStatement[2].executeUpdate()
            // delete from boards_users
            deleteStatement[1].setString(1, id.toString())
            deleteStatement[1].executeUpdate()
            // delete from boards
            deleteStatement[0].setString(1, id.toString())
            deleteStatement[0].executeUpdate() != 0 // returned
            // conn.commit() // if using transactions, also need to delay return value
        } catch (ex: SQLException) {
            println(ex.message)
            false
        }
    }
}