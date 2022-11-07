package frontend.services

import java.util.UUID
import io.ktor.client.request.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.http.*
import models.Item

object ItemService {
    private lateinit var client: HttpClient

    fun init(httpClient: HttpClient) {
        client = httpClient
    }
    suspend fun addItem(bid: UUID, item: Item): Item? {
        val result = client.post("board/${bid}/items") {
            contentType(ContentType.Application.Json)
            setBody(item)
        }
        return result.body()
    }

    suspend fun getItems(bid: UUID): List<Item> {
        val result = client.get("board/${bid}/items")

        return result.body()
    }

    suspend fun getItem(bid: UUID, id: UUID): Item {
        val result = client.get("board/${bid}/items") {
            url(id.toString())
        }
        return result.body()
    }

    suspend fun updateItem(bid: UUID, new: Item): Item? {
        val result = client.put("board/${bid}/items") {
            contentType(ContentType.Application.Json)
            setBody(new)
        }
        return result.body()
    }

    suspend fun deleteItem(bid: UUID, id: UUID) {
        val result = client.delete("board/${bid}/items") {
            url(id.toString())
        }
        if (result.status != HttpStatusCode.NoContent) error("failed to delete item")
    }
}