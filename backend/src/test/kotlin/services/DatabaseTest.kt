package services

import database.DatabaseConnection
import models.Board
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class DatabaseTest {

    @Test
    fun testDatabase() {
        val db = DatabaseConnection()

        var conn = db.connect()
        assert(conn != null)

        db.end(conn) // drop test table
        db.init(conn) // create test table and init

        val names: MutableSet<String> = db.query(conn)

        assertEquals(1, names.size)
        assertEquals("Juthika Hoque", names.elementAt(0))
    }

}