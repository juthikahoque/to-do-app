package models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class Item(
    val title: String = "",
    val dueDate: @Serializable(with = DateTimeSerializer::class) LocalDateTime? = null,
    val boardId: @Serializable(with = UUIDSerializer::class) UUID = UUID.randomUUID(),
    val labels: Set<Label> = setOf(),
    val priority: Int = 0,
    val id: @Serializable(with = UUIDSerializer::class) UUID = UUID.randomUUID(),
    val done: Boolean = false,
    val attachments: Set<Attachment> = setOf(),
    val owner: User,
    val description: String = "",
)
