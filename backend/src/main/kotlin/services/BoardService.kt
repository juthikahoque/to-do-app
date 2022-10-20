package services

import models.Board
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*

object BoardService {
    private lateinit var conn: Connection
    private lateinit var addStatement: PreparedStatement
    private lateinit var getStatement: PreparedStatement
    private lateinit var updateStatement: PreparedStatement
    private lateinit var deleteStatement: PreparedStatement

    fun init(connection: Connection) {
        conn = connection
        // create table if dne
        val statement = conn.createStatement()
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS boards (id TEXT, name TEXT, updated_at TEXT, created_at TEXT)")

        addStatement = conn.prepareStatement("INSERT INTO boards(id, name, updated_at, created_at) VALUES (?, ?, ?, ?)")
        getStatement = conn.prepareStatement("SELECT * FROM boards WHERE id = ?")
        updateStatement = conn.prepareStatement("UPDATE boards SET name = ?, updated_at = ? WHERE id = ?")
        deleteStatement = conn.prepareStatement("DELETE FROM boards WHERE id = ?")
    }

    fun addBoard(board: Board): Board {
        try {
            addStatement.setString(1, board.id.toString())
            addStatement.setString(2, board.name)
            addStatement.setString(3, board.updated_at.toString())
            addStatement.setString(4, board.created_at.toString())
            addStatement.executeUpdate()
            return board
        } catch (ex: SQLException) {
            error("sql error")
        }
    }

    fun getBoards(): List<Board> {
        try {
            val statement = conn.createStatement()

            val res = statement.executeQuery("SELECT * FROM boards")
            val list = mutableListOf<Board>()
            while (res.next()) {
                list.add(Board(
                    name = res.getString("name"),
                    users = mutableSetOf(),
                    id = UUID.fromString(res.getString("id")),
                    labels = mutableSetOf(),
                    updated_at = LocalDateTime.parse(res.getString("updated_at")),
                    created_at = LocalDateTime.parse(res.getString("created_at")),
                ))
            }
            res.close()
            return list
        } catch (ex: SQLException) {
            error("not found")
        }
    }

    fun getBoard(id: UUID): Board {
        try {
            getStatement.setString(1, id.toString())
            val res = getStatement.executeQuery()

            if (res.next()) {
                val board = Board(
                    name = res.getString("name"),
                    users = mutableSetOf(),
                    id = UUID.fromString(res.getString("id")),
                    labels = mutableSetOf(),
                    updated_at = LocalDateTime.parse(res.getString("updated_at")),
                    created_at = LocalDateTime.parse(res.getString("created_at")),
                )
                res.close()
                return board
            } else {
                error("not found")
            }
        } catch (ex: SQLException) {
            error("not found")
        }
    }

    fun updateBoard(new: Board): Board? {
        return try {
            updateStatement.setString(1, new.name)
            updateStatement.setString(2, LocalDateTime.now().toString())
            updateStatement.executeUpdate()
            new
        } catch (ex: SQLException) {
            print(ex.message)
            null
        }
    }

    fun deleteBoard(id: UUID): Boolean {
        return try {
            deleteStatement.setString(1, id.toString())
            deleteStatement.executeUpdate() != 0 // returned
        } catch (ex: SQLException) {
            println(ex.message)
            false
        }
    }
}