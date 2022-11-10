package backend.routes

import backend.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import backend.models.AuthUser


fun ApplicationTestBuilder.configureTest(mockUserId: String): HttpClient {
    environment {
        config = ApplicationConfig("test.conf")
    }
    application {
        configureRouting()
        mockAuth(mockUserId)
        configureErrorHandling()
        configureSerialization()
    }
    val client = createClient {
        this.install(ContentNegotiation) {
            json()
        }
    }
    return client
}

fun Application.mockAuth(mockUserId: String) {
    install(Authentication) {
        val provider = MockAuthProvider(null, mockUserId)
        register(provider)
    }
}

class MockAuthProvider internal constructor(name: String?, private val mockUserId: String) : AuthenticationProvider(
    Configuration(name)
) {
    class Configuration(name: String?) : Config(name)

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        context.principal(AuthUser(mockUserId))
    }
}