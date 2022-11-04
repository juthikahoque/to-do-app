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
import org.junit.jupiter.api.BeforeEach
import java.util.*

internal class ItemServiceTest {

    val bid = UUID.randomUUID()
    val i1 = Item(text = "item", id = bid)
    val i2 = Item(text = "2", id = bid)

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
                        if (request.url.encodedPath == "/board/${bid}/items") Json.encodeToString(listOf(i1, i2))
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

    @BeforeEach
    fun init() {
        ItemService.init(httpClient)
    }

    @Test
    fun addItem() {
        runBlocking {
            val result = ItemService.addItem(bid, i1)

            assertEquals(result, i1)
        }
    }

    @Test
    fun getItems() {
        runBlocking {
            val result = ItemService.getItems(bid)

            assertEquals(result, listOf(i1, i2))
        }
    }

    @Test
    fun getItem() {
        runBlocking {
            val result = ItemService.getItem(bid, i1.id)

            assertEquals(result, i1)
        }
    }

    @Test
    fun updateItem() {
        runBlocking {
            val result = ItemService.updateItem(bid, i1)

            assertEquals(result, i1)
        }
    }

    @Test
    fun deleteItem() {
        runBlocking {
            ItemService.deleteItem(bid, i1.id)
        }
    }
}