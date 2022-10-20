package models

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime
import java.util.*

internal class BoardTest {
    @Test
    fun serialize() {
        val userId = UUID.randomUUID()
        val board = Board("board", mutableSetOf(userId), UUID.randomUUID(), mutableSetOf(), LocalDateTime.now(), LocalDateTime.now())
        board.labels.add(Label("label"))

        val str = Json.encodeToString(board)
        val output = Json.decodeFromString<Board>(str)

        print(str)

        assertEquals(output, board)
    }
}