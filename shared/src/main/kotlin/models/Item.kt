package models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class Item(
    val text: String = "",
    val dueDate: @Serializable(with = DateTimeSerializer::class) LocalDateTime? = null,
    val boardId: @Serializable(with = UUIDSerializer::class) UUID = UUID.randomUUID(),
    val labels: MutableSet<Label> = mutableSetOf(),
    val priority: Int = 0,
    val id: @Serializable(with = UUIDSerializer::class) UUID = UUID.randomUUID(),
    val done: Boolean = false,
    val attachments: MutableSet<Attachment> = mutableSetOf(),
)
