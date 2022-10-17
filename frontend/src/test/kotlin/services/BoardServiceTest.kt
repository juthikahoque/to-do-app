package services

import models.Board
import models.Label
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*
import io.ktor.client.*
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class BoardServiceTest {

    @Mock
    val httpMock = HttpClient()
    val boardService = BoardService(httpMock)

    @Test
    fun addBoard() {
        val board = Board("board")

        
    }

    @Test
    fun getBoards() {
        
    }

    @Test
    fun getBoard() {
        
    }

    @Test
    fun updateBoard() {
        
    }

    @Test
    fun deleteBoard() {
        
    }
}