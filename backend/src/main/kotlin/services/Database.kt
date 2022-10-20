package database

import java.sql.*


class DatabaseConnection {

    fun connect(): Connection {
        try {
            val url = "jdbc:sqlite:todo.db"
            val conn = DriverManager.getConnection(url)
            println("Connection to SQLite has been established.")
            return conn
        } catch (e: SQLException) {
            println(e.message)
            error("cannot connect to database")
        }
    }
}