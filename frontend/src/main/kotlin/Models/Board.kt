package models

import java.util.UUID
import kotlinx.serialization.*

@Serializable
data class Board(
    val name: String,
    val users: MutableSet<@Serializable(with = UUIDSerializer::class) UUID>,
    val id: @Serializable(with = UUIDSerializer::class) UUID,
    val labels: MutableSet<Label>
) {
    constructor(name: String) : this(name, mutableSetOf(), UUID.randomUUID(), mutableSetOf())
}
