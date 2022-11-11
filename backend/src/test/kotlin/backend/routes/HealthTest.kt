package backend.routes

import backend.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class HealthTest {

    @Test
    fun testHealth() = testApplication {
        environment {
            config = ApplicationConfig("test.conf")
        }
        application {
            configureRouting()
            configureErrorHandling()
            configureSerialization()
        }

        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testAuth() = testApplication {
        environment {
            config = ApplicationConfig("test.conf")
        }

        val mockUserId = "user"

        application {
            configureRouting()
            mockAuth(mockUserId)
            configureErrorHandling()
            configureSerialization()
        }


        val response = client.get("/auth")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(mockUserId, response.body<String>())
    }
}
