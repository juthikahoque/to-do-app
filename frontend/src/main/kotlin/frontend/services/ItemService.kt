package frontend.services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import models.Attachment
import models.Item
import models.Label
import java.io.File
import java.time.LocalDateTime
import java.util.*

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

    suspend fun orderItem(bid: UUID, from: Int, to: Int) {
        val result = client.put("board/$bid/items/order") {
            parameter("from", from)
            parameter("to", to)
        }
    }

    suspend fun filterByDates(bid: UUID, startDate: LocalDateTime, endDate: LocalDateTime? = null): List<Item> {
        val headers = Parameters.build {
            append("date", startDate.toString())
            append("date", endDate?.toString() ?: "")
        }.formUrlEncode()
        val result = client.get("board/${bid}/items?${headers}")
        return result.body()
    }

    suspend fun filterByLabels(bid: UUID, labels: MutableSet<Label>) : List<Item> {
        val headers = Parameters.build {
            for(label in labels) {
                append("label", label.value)
            }
        }.formUrlEncode()
        val result = client.get("board/${bid}/items?${headers}")
        return result.body()
    }
    suspend fun filterByPriorities(bid: UUID, priorities: MutableSet<Int>) : List<Item> {
        val headers = Parameters.build {
            for(priority in priorities) {
                append("priority", priority.toString())
            }
        }.formUrlEncode()
        val result = client.get("board/${bid}/items?${headers}")

        return result.body()
    }

    suspend fun uploadFile(bid: UUID, id: UUID, file: File) {
        client.submitFormWithBinaryData(
            url = "board/$bid/items/$id/file",
            formData = formData {
                append("file", file.readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, ContentType.Application.OctetStream)
                    append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                })
            }
        )
    }

    suspend fun downloadFile(bid: UUID, id: UUID, attachment: Attachment): ByteArray {
        val name = attachment.name
        val response = client.get("board/$bid/items/$id/file/$name")
        return response.body()
    }
}