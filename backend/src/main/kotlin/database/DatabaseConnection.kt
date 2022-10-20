package database

import java.sql.*


class DatabaseConnection {

    fun connect(): Connection? {
        var conn: Connection? = null
        try {
            val url = "jdbc:sqlite:todo.db"
            conn = DriverManager.getConnection(url)
            println("Connection to SQLite has been established.")
        } catch (e: SQLException) {
            println(e.message)
        }
        return conn
    }

    // only call this function ONCE
    fun init(conn:Connection?) {
        try {
            if (conn != null) {
                val stmt = conn.createStatement()
                var sql = "CREATE TABLE test (name VARCHAR(1000))"
                stmt.executeUpdate(sql)

                println("Created table in given database...")

                sql = "INSERT INTO test VALUES ('Juthika Hoque')"
                stmt.executeUpdate(sql)

                println("inserted into test")
            }
        } catch (ex: SQLException) {
            println(ex.message)
        }
    }

    fun end(conn:Connection?) {
        try {
            if (conn != null) {
                val stmt = conn.createStatement()
                val sql = "DROP TABLE IF EXISTS test"
                stmt.executeUpdate(sql)

                println("Drop table in given database...")
            }
        } catch (ex: SQLException) {
            println(ex.message)
        }
    }

    fun query(conn: Connection?): MutableSet<String> {
        try {
            var names = mutableSetOf<String>()
            if (conn != null) {
                val stmt = conn.createStatement()
                val sql = "select * from test"
                val results = stmt.executeQuery(sql)

                while(results.next()) {
                    val name = results.getString("name")
                    names.add(name)
                }
            }
            return names
        } catch (ex: SQLException) {
            println(ex.message)
            return mutableSetOf()
        }
    }

}