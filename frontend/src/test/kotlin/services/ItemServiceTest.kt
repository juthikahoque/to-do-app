package services

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Item
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.*

internal class ItemServiceTest {

    val bid = UUID.randomUUID()
    val i1 = Item("item", bid)
    val i2 = Item("2", bid)

    private val httpClient = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                when (request.method) {
                    HttpMethod.Post -> respond(
                        Json.encodeToString(i1),
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, "application/json")
                    )

                    HttpMethod.Get -> respond(
                        if (request.url.encodedPath == "/board/${bid}/item") Json.encodeToString(listOf(i1, i2))
                        else Json.encodeToString(i1),
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, "application/json")
                    )

                    HttpMethod.Put -> respond(
                        Json.encodeToString(i1),
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, "application/json")
                    )

                    HttpMethod.Delete -> respond("", HttpStatusCode.NoContent)

                    else -> error("unhandled")
                }
            }
        }
        install(ContentNegotiation) {
            json()
        }
    }
    private val itemService = ItemService(httpClient)

    @Test
    fun addItem() {
        runBlocking {
            val result = itemService.addItem(bid, i1)

            assertEquals(result, i1)
        }
    }

    @Test
    fun getItems() {
        runBlocking {
            val result = itemService.getItems(bid)

            assertEquals(result, listOf(i1, i2))
        }
    }

    @Test
    fun getItem() {
        runBlocking {
            val result = itemService.getItem(bid, i1.id)

            assertEquals(result, i1)
        }
    }

    @Test
    fun updateItem() {
        runBlocking {
            val result = itemService.updateItem(bid, i1)

            assertEquals(result, i1)
        }
    }

    @Test
    fun deleteItem() {
        runBlocking {
            val result = itemService.deleteItem(bid, i1.id)
        }
    }
}