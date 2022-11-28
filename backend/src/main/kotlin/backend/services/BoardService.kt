package backend.services

import models.*
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*
import kotlin.math.max
import kotlin.math.min

object BoardService {
    private lateinit var conn: Connection

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
                ordering INT NOT NULL,
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
    }

    private val removeUser by lazy {
        listOf(
            conn.prepareStatement(
                """
                UPDATE boards_users
                SET ordering = boards_users.ordering - 1
                FROM (SELECT * FROM boards_users WHERE boardId = ? AND userId LIKE ?) temp
                WHERE temp.userId = boards_users.userId AND temp.ordering < boards_users.ordering
                """.trimIndent()
            ),
            conn.prepareStatement(
                """
                DELETE FROM boards_users WHERE boardId = ? AND userId LIKE ?
                """.trimIndent()
            )
        )
    }

    private fun removeUser(boardId: String, userId: String) {
        try {
            // update ordering
            removeUser[0].setString(1, boardId)
            removeUser[0].setString(2, userId)
            removeUser[0].execute()
            // remove entry
            removeUser[1].setString(1, boardId)
            removeUser[1].setString(2, userId)
            removeUser[1].execute()
        } catch (ex: SQLException) {
            appError(ex.message?:"sql remove user from boards failed")
        }
    }

    private val addUser by lazy {
        conn.prepareStatement(
            """
            INSERT INTO boards_users(boardId, userId, ordering) 
            SELECT ?, ?, COALESCE(MAX(ordering) + 1, 0) FROM boards_users WHERE userId = ?
            """.trimIndent()
        )
    }

    private fun addUser(boardId: String, userId: String) {
        try {
            // add entry
            addUser.setString(1, boardId)
            addUser.setString(2, userId)
            addUser.setString(3, userId)
            addUser.executeUpdate()
        } catch (ex: SQLException) {
            appError(ex.message?:"sql add user to boards failed")
        }
    }

    private val addLabel by lazy {
        conn.prepareStatement("""INSERT INTO boards_labels(boardId, label) VALUES (?, ?)""")
    }

    private fun addLabel(boardId: String, label: String) {
        try {
            // add entry
            addLabel.setString(1, boardId)
            addLabel.setString(2, label)
            addLabel.executeUpdate()
        } catch (ex: SQLException) {
            appError(ex.message?:"sql add label to boards failed")
        }
    }

    private val removeLabel by lazy {
        conn.prepareStatement("""DELETE FROM boards_labels WHERE boardId = ? AND label LIKE ?""")
    }

    private fun removeLabel(boardId: String, label: String) {
        try {
            // remove entry
            removeLabel.setString(1, boardId)
            removeLabel.setString(2, label)
            removeLabel.execute()
        } catch (ex: SQLException) {
            appError(ex.message?:"sql remove label from boards failed")
        }
    }

    private val getFields by lazy {
        listOf(
            conn.prepareStatement("SELECT * FROM boards_users WHERE boardId = ?"),
            conn.prepareStatement("SELECT * FROM boards_labels WHERE boardId = ?"),
        )
    }

    /**
     * create a board from a result set, add userId and Labels
     */
    private fun getBoardFromRes(res: ResultSet): Board {
        val boardId = res.getString("id")
        // get board users
        getFields[0].setString(1, boardId)
        val usersRes = getFields[0].executeQuery()
        val users = mutableSetOf<User>()
        while (usersRes.next()) {
            users.add(UserService.getUserById(usersRes.getString("userId")))
        }
        // get board labels
        getFields[1].setString(1, boardId)
        val labelsRes = getFields[1].executeQuery()
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

    private val addBoard by lazy {
        conn.prepareStatement("INSERT INTO boards(id, name, updated_at, created_at) VALUES (?, ?, ?, ?)")
    }

    fun addBoard(board: Board): Board {
        try {
            val boardIdStr = board.id.toString()
            // insert into boards
            addBoard.setString(1, board.id.toString())
            addBoard.setString(2, board.name)
            addBoard.setString(3, board.updated_at.toString())
            addBoard.setString(4, board.created_at.toString())
            addBoard.executeUpdate()
            // insert into boards_users
            for (user in board.users) {
                addUser(boardIdStr, user.userId)
            }
            // insert into boards_labels
            for (label in board.labels) {
                addLabel(boardIdStr, label.value)
            }
            // conn.commit() // if using transactions
            return board
        } catch (ex: SQLException) {
            appError(ex.message?:"sql add boards failed")
        }
    }


    private val getBoards by lazy {
        conn.prepareStatement(
            """
            SELECT * FROM boards 
            INNER JOIN boards_users ON id = boardId AND userId = ?
            ORDER BY ordering ASC
            """.trimIndent()
        )
    }

    fun getBoards(userId: String): List<Board> {
        try {
            getBoards.setString(1, userId)
            val res = getBoards.executeQuery()
            val list = mutableListOf<Board>()
            while (res.next()) {
                list.add(getBoardFromRes(res))
            }
            res.close()
            return list
        } catch (ex: SQLException) {
            appError(ex.message?:"sql get boards failed", AppError.NotFound)
        }
    }

    private val getBoard by lazy {
        conn.prepareStatement("""SELECT * FROM boards WHERE id = ?""")
    }

    fun getBoard(id: UUID): Board {
        try {
            getBoard.setString(1, id.toString())

            val res = getBoard.executeQuery()
            if (res.next()) {
                val board = getBoardFromRes(res)
                res.close()
                return board
            } else {
                appError("sql no board with $id found", AppError.NotFound)
            }
        } catch (ex: SQLException) {
            appError(ex.message?:"sql get $id board failed")
        }
    }

    private val updateBoard by lazy {
        conn.prepareStatement("UPDATE boards SET name = ?, updated_at = ? WHERE id = ?")
    }

    fun updateBoard(new: Board): Board? {
        return try {
            val boardIdString = new.id.toString()
            val old = getBoard(new.id)
            // update boards
            updateBoard.setString(1, new.name)
            updateBoard.setString(2, new.updated_at.toString())
            updateBoard.setString(3, boardIdString)
            updateBoard.executeUpdate()
            // update boards_users
            val users = new.users.toMutableSet()
            for (oldUser in old.users) {
                if (!users.remove(oldUser)) {
                    removeUser(boardIdString, oldUser.userId)
                }
            }
            for (unAddedUsers in users) {
                addUser(new.id.toString(), unAddedUsers.userId)
            }
            // update boards_labels
            val labels = new.labels.toMutableSet()
            for (oldLabel in old.labels) {
                if (!labels.remove(oldLabel)) {
                    removeLabel(boardIdString, oldLabel.value)
                }
            }
            for (unAddedLabel in labels) {
                addLabel(boardIdString, unAddedLabel.value)
            }
            // conn.commit() // if using transactions
            new
        } catch (ex: SQLException) {
            appError(ex.message?:"sql update board failed")
        }
    }

    private val deleteBoard by lazy {
        conn.prepareStatement("DELETE FROM boards WHERE id = ?")
    }

    fun deleteBoard(id: UUID): Boolean {
        return try {
            // delete from boards_users
            removeUser(id.toString(), "%")
            // delete from boards_labels
            removeLabel(id.toString(), "%")
            // delete from boards
            deleteBoard.setString(1, id.toString())
            deleteBoard.executeUpdate() != 0 // returned

            // conn.commit() // if using transactions, also need to delay return value
        } catch (ex: SQLException) {
            appError(ex.message?:"sql delete $id board failed")
        }
    }

    private val changeOrderSQL by lazy {
        listOf(
            conn.prepareStatement("UPDATE boards_users SET ordering = -1 WHERE userId = ? AND ordering = ?"),
            conn.prepareStatement("UPDATE boards_users SET ordering = ordering + ? WHERE userId = ? AND ordering BETWEEN ? AND ?"),
            conn.prepareStatement("UPDATE boards_users SET ordering = ? WHERE userId = ? AND ordering = -1"),
        )
    }
    fun changeOrder(userId: String, from: Int, to: Int) {
        try {
            changeOrderSQL[0].setString(1, userId)
            changeOrderSQL[1].setString(2, userId)
            changeOrderSQL[2].setString(2, userId)

            changeOrderSQL[0].setInt(2, from)
            changeOrderSQL[1].setInt(1, if (from < to) -1 else 1)
            changeOrderSQL[1].setInt(3, min(from, to))
            changeOrderSQL[1].setInt(4, max(to, from))
            changeOrderSQL[2].setInt(1, to)

            changeOrderSQL.forEach { it.executeUpdate() }
        } catch (ex: SQLException) {
            appError(ex.message?:"sql boards change order failed")
        }
    }
}
