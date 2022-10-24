package services

import com.google.api.client.googleapis.auth.oauth2.GoogleBrowserClientRequestUrl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.net.URI

suspend fun main() {
    val x = GoogleBrowserClientRequestUrl(
        "327753002566-lhlt4uqme7g7qroth3h3qudsccn0vhhl.apps.googleusercontent.com",
        "http://localhost:5000/oauth-authorized/google", listOf(
            "openid",
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
        )
    ).setResponseTypes(listOf("code")).build()

    Desktop.getDesktop().browse(URI(x))

//    val client = HttpClient() {
//        install(ContentNegotiation) {
//            json(Json{ ignoreUnknownKeys = true })
//        }
//        defaultRequest {
//            url("http://127.0.0.1:8080")
//        }
//    }
//
//    val z = client.get(x).body<String>()

//    print(z)
}