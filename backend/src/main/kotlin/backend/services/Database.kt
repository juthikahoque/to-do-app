package backend.services

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class Database {

    fun connect(db: String): Connection {
        try {
            val url = "jdbc:sqlite:${db}.db"
            val conn = DriverManager.getConnection(url)
            println("Connection to SQLite has been established.")
            return conn
        } catch (e: SQLException) {
            println(e.message)
            error("cannot connect to database")
        }
    }
}