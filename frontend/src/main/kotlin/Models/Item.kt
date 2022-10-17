package models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class Item(
    val text: String,
    val dueDate: @Serializable(with = DateTimeSerializer::class) LocalDateTime,
    val boardId: @Serializable(with = UUIDSerializer::class) UUID,
    val labels: MutableSet<Label>,
    val priority: Int,
    val id: @Serializable(with = UUIDSerializer::class) UUID,
    val done: Boolean,
)
