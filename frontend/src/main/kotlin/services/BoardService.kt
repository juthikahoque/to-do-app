package services

import java.util.UUID
import models.*
import io.ktor.client.request.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.http.*

class BoardService(private val client: HttpClient) {
    suspend fun addBoard(board: Board): Board? {
        val result = client.post("board") {
            contentType(ContentType.Application.Json)
            setBody(board)
        }
        return result.body()
    }

    suspend fun getBoards(): List<Board> {
        val result = client.get("board")

        return result.body()
    }

    suspend fun getBoard(id: UUID): Board {
        val result = client.get("board") {
            url(id.toString())
        }
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
        val result = client.delete("board") {
            url(id.toString())
        }
    }
}