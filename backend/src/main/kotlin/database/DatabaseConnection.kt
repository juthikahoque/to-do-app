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

    fun query(conn:Connection?): MutableSet<String> {
        var names = mutableSetOf<String>()
        try {
            if (conn != null) {
                val stmt = conn.createStatement()
                var sql = "CREATE TABLE test (name VARCHAR(1000))"
                stmt.executeUpdate(sql)

                println("Created table in given database...")

                val insert = conn.createStatement()
                sql = "INSERT INTO test VALUES ('Juthika Hoque')"
                insert.executeUpdate(sql)

                sql = "select * from test"
                val query = conn.createStatement()
                val results = query.executeQuery(sql)

                println("Fetched data:")
                var names = mutableSetOf<String>()
                while (results.next()) {
                    val name = results.getString("name")
                    names.add(name)
                }
            }
            return names
        } catch (ex: SQLException) {
            println(ex.message)
            return names
        }
    }

}