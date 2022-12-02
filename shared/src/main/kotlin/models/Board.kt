package models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class Board(
    var name: String = "",
    val users: Set<User> = setOf(),
    val labels: Set<Label> = setOf(),
    val updated_at: @Serializable(with = DateTimeSerializer::class) LocalDateTime = LocalDateTime.now(),
    val created_at: @Serializable(with = DateTimeSerializer::class) LocalDateTime = LocalDateTime.now(),
    val id: @Serializable(with = UUIDSerializer::class) UUID = UUID.randomUUID(),
)
