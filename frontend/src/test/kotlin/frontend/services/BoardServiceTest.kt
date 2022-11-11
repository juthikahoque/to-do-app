package frontend.services

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Board
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BoardServiceTest {
    val b1 = Board("board")
    val b2 = Board("2")

    private val httpClient = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                when (request.method) {
                    HttpMethod.Post -> respond(
                        Json.encodeToString(b1),
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, "application/json")
                    )

                    HttpMethod.Get -> respond(
                        if (request.url.encodedPath == "/board") Json.encodeToString(listOf(b1, b2))
                        else Json.encodeToString(b1),
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, "application/json")
                    )

                    HttpMethod.Put -> respond(
                        if (request.url.encodedPath == "/board") Json.encodeToString(b1)
                        else "",
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
        BoardService.init(httpClient)
    }

    @Test
    fun addBoard() {
        runBlocking {
            val result = BoardService.addBoard(b1)

            assertEquals(result, b1)
        }
    }

    @Test
    fun getBoards() {
        runBlocking {
            val result = BoardService.getBoards()

            assertEquals(result, listOf(b1, b2))
        }
    }

    @Test
    fun getBoard() {
        runBlocking {
            val result = BoardService.getBoard(b1.id)

            assertEquals(result, b1)
        }
    }

    @Test
    fun updateBoard() {
        runBlocking {
            val result = BoardService.updateBoard(b1)

            assertEquals(result, b1)
        }
    }

    @Test
    fun deleteBoard() {
        runBlocking {
            BoardService.deleteBoard(b1.id)
        }
    }

    @Test
    fun orderBoard() {
        runBlocking {
            BoardService.orderBoard(5, 4)
        }
    }
}
