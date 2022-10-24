package models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class Item(
    val text: String = "default",
    val dueDate: @Serializable(with = DateTimeSerializer::class) LocalDateTime? = LocalDateTime.MAX,
    val boardId: @Serializable(with = UUIDSerializer::class) UUID = UUID.randomUUID(),
    val labels: MutableSet<Label> = mutableSetOf(),
    val priority: Int = 0,
    val id: @Serializable(with = UUIDSerializer::class) UUID = UUID.randomUUID(),
    val done: Boolean = false,
)
