package services

import java.util.UUID
import models.*
import io.ktor.client.request.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.http.*

class ItemService(private val client: HttpClient) {
    suspend fun addItem(bid: UUID, item: Item): Item? {
        val result = client.post("board/${bid}/item") {
            contentType(ContentType.Application.Json)
            setBody(item)
        }
        return result.body()
    }

    suspend fun getItems(bid: UUID): List<Item> {
        val result = client.get("board/${bid}/item")

        return result.body()
    }

    suspend fun getItem(bid: UUID, id: UUID): Item {
        val result = client.get("board/${bid}/item") {
            url(id.toString())
        }
        return result.body()
    }

    suspend fun updateItem(bid: UUID, new: Item): Item? {
        val result = client.put("board/${bid}/item") {
            contentType(ContentType.Application.Json)
            setBody(new)
        }
        return result.body()
    }

    suspend fun deleteItem(bid: UUID, id: UUID) {
        val result = client.delete("board/${bid}/item") {
            url(id.toString())
        }
    }
}