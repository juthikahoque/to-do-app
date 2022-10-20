package models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class Board(
    val name: String,
    val users: MutableSet<@Serializable(with = UUIDSerializer::class) UUID>,
    val id: @Serializable(with = UUIDSerializer::class) UUID,
    val labels: MutableSet<Label>,
    val updated_at: @Serializable(with = DateTimeSerializer::class) LocalDateTime,
    val created_at: @Serializable(with = DateTimeSerializer::class) LocalDateTime,
) {
    constructor(name: String) : this(name, mutableSetOf(), UUID.randomUUID(), mutableSetOf(), LocalDateTime.now(), LocalDateTime.now())
}
