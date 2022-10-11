import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

class Main {
    val client = HttpClient() {
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            url("http://127.0.0.1:8080")

        }
    }
}