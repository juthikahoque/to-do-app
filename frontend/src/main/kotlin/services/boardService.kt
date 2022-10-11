package services

import java.util.UUID
import models.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.http.*

class BoardService(client: HttpClient) {
    val client = client
    val boards = mutableSetOf(Board("hello"), Board("world"))

    suspend fun addBoard(board: Board): Board? {
        val result = client.post("board") {
            setBody(board)
        }
        if (result.status == HttpStatusCode.OK) {
            // assert board
            boards.add(board)
            return board
        }
        return null
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
            setBody(new)
        }
        if (result.status == HttpStatusCode.OK) {
            // assert board
            val removed = boards.removeIf { it.id == new.id }
            if (removed) {
                boards.add(new)
                return result.body()
            }
        }
        return null
    }

    suspend fun deleteBoard(id: UUID) {
        val result = client.delete("board") {
            url(id.toString())
        }
        if (result.status == HttpStatusCode.NoContent) {
            boards.removeIf {it.id == id}
        }
    }
}