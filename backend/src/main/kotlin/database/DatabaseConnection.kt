package database

import java.sql.*

class DatabaseConnection {

    fun connect(dbName: String): Connection? {
        var conn: Connection? = null
        try {
            val url = "jdbc:sqlite:${dbName}.db"
            conn = DriverManager.getConnection(url)
            println("Connection to SQLite has been established.")
        } catch (e: SQLException) {
            println(e.message)
        }
        return conn
    }

}