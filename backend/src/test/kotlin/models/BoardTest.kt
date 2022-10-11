package models

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.*

internal class BoardTest {

    @Test
    fun getId() {
        val board = Board("board")

        assertNotNull(board.id)
    }

    @Test
    fun getLabels() {
        val board = Board("board", mutableSetOf(), UUID.randomUUID(), mutableSetOf(Label("label")))

        assertNotNull(board.labels)
        assertTrue(board.labels.contains(Label("label")))
    }

    @Test
    fun addLabels() {
        val board = Board("board")
        val label = Label("label")

        board.labels.add(label)

        assertNotNull(board.labels)
        assertTrue(board.labels.contains(label))
    }

    @Test
    fun getName() {
        val board = Board("board")

        assertEquals("board", board.name)
    }

    @Test
    fun getUsers() {
        val userId = UUID.randomUUID()
        val board = Board("board", mutableSetOf(userId), UUID.randomUUID(), mutableSetOf())

        assertTrue(board.users.contains(userId))
    }

    @Test
    fun addUsers() {
        val board = Board("board")
        val userId = UUID.randomUUID()

        board.users.add(userId)

        assertTrue(board.users.contains(userId))
    }

    @Test
    fun serialize() {
        val userId = UUID.randomUUID()
        val board = Board("board", mutableSetOf(userId), UUID.randomUUID(), mutableSetOf())
        board.labels.add(Label("label"))

        val str = Json.encodeToString(board)
        val output = Json.decodeFromString<Board>(str)

        print(str)

        assertEquals(output, board)
    }
}