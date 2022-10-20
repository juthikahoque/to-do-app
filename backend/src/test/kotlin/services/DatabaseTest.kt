package services

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class DatabaseTest {

    @Test
    fun testDatabase() {
        val db = Database()
        var conn = db.connect()

        assertNotNull(conn)
    }
}