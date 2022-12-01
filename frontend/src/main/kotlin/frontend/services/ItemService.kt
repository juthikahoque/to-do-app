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
        val result = client.post("board/$bid/items") {
            contentType(ContentType.Application.Json)
            setBody(item)
        }
        return result.body()
    }

    suspend fun getItems(
        bid: String,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        labels: MutableSet<Label> = mutableSetOf(),
        priorities: MutableSet<Int> = mutableSetOf(),
        search: String = "",
        sortBy: String = "",
        orderBy: String = "",
    ): MutableList<Item> {
        val result = client.get("board/$bid/items") {
            url {
                if (startDate != null)  parameters.append("sDate", startDate.toString())
                if (endDate != null)    parameters.append("eDate", endDate.toString())
                for (p in priorities)   parameters.append("priority", p.toString())
                for (l in labels)       parameters.append("label", l.value)
                if (search != "")       parameters.append("search", search)
                if (sortBy != "")       parameters.append("sortBy", sortBy)
                if (orderBy != "")      parameters.append("orderBy", orderBy)
            }
        }
        return result.body()
    }

    suspend fun getItem(bid: UUID, id: UUID): Item {
        val result = client.get("board/$bid/items/$id")
        return result.body()
    }

    suspend fun updateItem(bid: UUID, new: Item): Item? {
        val result = client.put("board/$bid/items") {
            contentType(ContentType.Application.Json)
            setBody(new)
        }
        return result.body()
    }

    suspend fun deleteItem(bid: UUID, id: UUID) {
        val result = client.delete("board/$bid/items/$id")
        if (result.status != HttpStatusCode.NoContent) error("failed to delete item")
    }

    suspend fun orderItem(bid: UUID, from: Int, to: Int): MutableList<Item> {
        val result = client.put("board/$bid/items/order") {
            parameter("from", from)
            parameter("to", to)
        }
        return result.body()
    }

    suspend fun uploadFile(bid: UUID, id: UUID, file: File) {
        val name = Base64.getUrlEncoder().encodeToString(file.name.toByteArray())
        client.submitFormWithBinaryData(
            url = "board/$bid/items/$id/file",
            formData = formData {
                append("file", file.readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, ContentType.Application.OctetStream)
                    append(HttpHeaders.ContentDisposition, "filename=$name")
                })
            }
        )
    }

    suspend fun downloadFile(bid: UUID, id: UUID, attachment: Attachment): ByteArray {
        val name = Base64.getUrlEncoder().encodeToString(attachment.name.toByteArray())
        val response = client.get("board/$bid/items/$id/file/$name")
        return response.body()
    }

    suspend fun deleteFile(bid: UUID, id: UUID, attachment: Attachment) {
        val name = Base64.getUrlEncoder().encodeToString(attachment.name.toByteArray())
        client.delete("board/$bid/items/$id/file/$name")
    }
}