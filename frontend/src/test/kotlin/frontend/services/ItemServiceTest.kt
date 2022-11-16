package frontend.services

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Attachment
import models.Item
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

internal class ItemServiceTest {

    val bid = UUID.randomUUID()
    val i1 = Item(text = "item", id = bid)
    val i2 = Item(text = "2", id = bid)

    private fun MockRequestHandleScope.mockPost(request: HttpRequestData): HttpResponseData {
        return when (request.url.encodedPath) {
            "/board/$bid/items" -> respond(
                Json.encodeToString(i1),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
            "/board/$bid/items/${i1.id}/file" -> respond(
                "",
                HttpStatusCode.OK,
            )
            else -> error("not handled")
        }
    }

    private fun MockRequestHandleScope.mockGet(request: HttpRequestData): HttpResponseData {
        return when (request.url.encodedPath) {
            "/board/${bid}/items" -> respond(
                Json.encodeToString(listOf(i1, i2)),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
            "/board/$bid/items/${i1.id}/file/build.gradle" -> respond(
                File("build.gradle").readBytes(),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/octet-stream")
            )
            else -> respond(
                Json.encodeToString(i1),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
    }

    private val httpClient = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                when (request.method) {
                    HttpMethod.Post -> mockPost(request)

                    HttpMethod.Get -> mockGet(request)

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

    @Test
    fun uploadFile() {
        runBlocking {
            ItemService.uploadFile(bid, i1.id, File("build.gradle"))
        }
    }

    @Test
    fun downloadFile() {
        runBlocking {
            val data = ItemService.downloadFile(bid, i1.id, Attachment("build.gradle"))

            // compare byte array are equal
            File("build.gradle").readBytes().forEachIndexed { idx, byte -> assertEquals(byte, data[idx]) }
        }
    }
}
