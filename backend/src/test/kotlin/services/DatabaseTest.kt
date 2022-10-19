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

        val names: MutableSet<String> = db.query(conn)

        assertEquals(names.size, 1)
        assertEquals(names.elementAt(0), "Juthika Hoque")
    }


}