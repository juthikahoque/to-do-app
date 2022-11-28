package frontend.services

import java.util.UUID
import io.ktor.client.request.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.http.*
import models.Board

object BoardService {

    private lateinit var client: HttpClient

    fun init(httpClient: HttpClient) {
        client = httpClient
    }

    suspend fun addBoard(board: Board): Board? {
        val result = client.post("board") {
            contentType(ContentType.Application.Json)
            setBody(board)
        }

        return result.body()
    }

    suspend fun getBoards(): List<Board> {
        val result = client.get("board")
        //print(result.body<String>())
        return result.body()
    }

    suspend fun getBoard(id: UUID): Board {
        val result = client.get("board/$id")
        return result.body()
    }

    suspend fun updateBoard(new: Board): Board? {
        val result = client.put("board") {
            contentType(ContentType.Application.Json)
            setBody(new)
        }
        return result.body()
    }

    suspend fun deleteBoard(id: UUID) {
        val result = client.delete("board/$id")
        if (result.status != HttpStatusCode.NoContent) error("failed to delete item")
    }

    suspend fun orderBoard(from: Int, to: Int) {
        val result = client.put("board/order") {
            parameter("from", from)
            parameter("to", to)
        }
    }
}