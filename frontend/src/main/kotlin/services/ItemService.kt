package services

import java.time.LocalDateTime
import java.util.*
import models.*
import io.ktor.client.request.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.http.*
import kotlinx.serialization.Serializable



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

    suspend fun filterByDates(bid: UUID, startDate: LocalDateTime, endDate: LocalDateTime? = null): List<Item> {
        val headers = Parameters.build {
            append("date", startDate.toString())
            append("date", endDate?.toString() ?: "")
        }.formUrlEncode()
        val result = client.get("board/${bid}/items?date=${headers}")
        return result.body()
    }

    suspend fun filterByLabels(bid: UUID, labels: MutableSet<Label>) : List<Item> {
        val headers = Parameters.build {
            for(label in labels) {
                append("priority", label.value)
            }
        }.formUrlEncode()
        val result = client.get("board/${bid}/items?label=${headers}")
        return result.body()
    }
    suspend fun filterByPriorities(bid: UUID, priorities: MutableSet<Int>) : List<Item> {
        val headers = Parameters.build {
            for(priority in priorities) {
                append("priority", priority.toString())
            }
        }.formUrlEncode()
        val result = client.get("board/${bid}/items?priority=${headers}")

        return result.body()
    }

}