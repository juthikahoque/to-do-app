package models

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class BoardTest {
    @Test
    fun serialize() {
        val board = Board(
            name = "board",
            users = mutableSetOf("uid 1", "uid 2"),
            id = UUID.randomUUID(),
            labels = mutableSetOf(Label("label1"), Label("label2")),
            updated_at = LocalDateTime.now(),
            created_at = LocalDateTime.now(),
        )

        val str = Json.encodeToString(board)
        val output = Json.decodeFromString<Board>(str)

        val usersString = board.users.fold("") { acc, uuid -> "${acc},\"${uuid}\"" }.drop(1)
        assertEquals(
            """{"name":"board","users":[${usersString}],"labels":[{"value":"label1"},{"value":"label2"}],"updated_at":"${board.updated_at}","created_at":"${board.created_at}","id":"${board.id}"}""",
            str
        )
        assertEquals(output, board)
    }
}