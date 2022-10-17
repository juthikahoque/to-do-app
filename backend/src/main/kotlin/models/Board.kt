package models

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Board(
    val name: String,
    val users: MutableSet<@Serializable(with = UUIDSerializer::class) UUID>,
    val id: @Serializable(with = UUIDSerializer::class) UUID,
    val labels: MutableSet<Label>
) {
    constructor(name: String) : this(name, mutableSetOf(), UUID.randomUUID(), mutableSetOf())
}
