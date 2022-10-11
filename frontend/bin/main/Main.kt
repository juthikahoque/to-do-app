
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
            url("localhost:8080")
            
        }
    }
}