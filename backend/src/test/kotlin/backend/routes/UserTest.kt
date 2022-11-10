package backend.routes

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import models.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import backend.services.*
import java.sql.DriverManager

class UserTest {

    @BeforeEach
    fun init() {
        Assertions.assertDoesNotThrow {
            val url = "jdbc:sqlite:test.db"
            conn = DriverManager.getConnection(url)
            // clear table
            val stat = conn.createStatement()
            stat.executeUpdate("DROP TABLE IF EXISTS boards")
            stat.executeUpdate("DROP TABLE IF EXISTS boards_users")
            stat.executeUpdate("DROP TABLE IF EXISTS boards_labels")

            BoardService.init(conn)
        }
    }

    @Test
    fun testGet() = testApplication {
        val client = configureTest("user")

        val string = Parameters.build {
            append("emails", "test@email.com")
            append("emails", "test@email.com")
        }.formUrlEncode()
        val response = client.get("/user?${string}")

        val expected = User(
            userId = "rIliX3UCwhY7qvdPeh0jJsQL1UR2",
            name = "test",
            email = "test@email.com",
        )
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(listOf(expected, expected), response.body<List<User>>())
    }

    @Test
    fun testGetSelf() = testApplication {
        val client = configureTest("rIliX3UCwhY7qvdPeh0jJsQL1UR2")

        val response = client.get("/user")

        val expected = User(
            userId = "rIliX3UCwhY7qvdPeh0jJsQL1UR2",
            name = "test",
            email = "test@email.com",
        )
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(expected, response.body<User>())
    }

}