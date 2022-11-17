package frontend.services

import java.util.UUID
import io.ktor.client.request.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.http.*
import models.User

object UserService {

    private lateinit var client: HttpClient

    fun init(httpClient: HttpClient) {
        client = httpClient
    }

    suspend fun getUserByEmail(email: String): List<User> {
        val result = client.get("user?emails=$email") {
        }
        return result.body()
    }
}