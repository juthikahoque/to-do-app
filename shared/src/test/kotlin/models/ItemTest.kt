package models

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class ItemTest {
    @Test
    fun serialize() {
        val item = Item(
            title = "item",
            dueDate = LocalDateTime.now(),
            boardId = UUID.randomUUID(),
            labels = mutableSetOf(Label("label1"), Label("label2")),
            priority = 1,
            id = UUID.randomUUID(),
            owner = User("user"),
            done = true,
            description = "des",
        )

        val str = Json.encodeToString(item)

        assertEquals(
            """{"title":"item","dueDate":"${item.dueDate}","boardId":"${item.boardId}","labels":[{"value":"label1"},{"value":"label2"}],"priority":1,"id":"${item.id}","done":true,"owner":${Json.encodeToString(item.owner)},"description":"des"}""",
            str
        )

        val output = Json.decodeFromString<Item>(str)
        assertEquals(output, item)
    }
}