package models

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.*


data class Item(
    val text: String,
    val dueDate: LocalDate,
    val boardId: @Serializable(with = UUIDSerializer::class) UUID,
    val labels: MutableSet<Label>,
    val priority: Int,
    val id: UUID,
)
