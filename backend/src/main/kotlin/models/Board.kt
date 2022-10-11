package models

import java.util.UUID

data class Board(
    val name: String,
    val users: MutableSet<UUID>,
) {
    val id: UUID = UUID.randomUUID()
    val labels: MutableSet<Label> = mutableSetOf()

    fun addUser(user: UUID) {
        users.add(user)
    }

    fun addTag(label: Label) {
        labels.add(label)
    }
}