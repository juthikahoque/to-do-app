package models

import java.time.LocalDate
import java.util.UUID


data class Item(
    var text: String,
    var dueDate: LocalDate,
    var boardId: UUID,
    var labels: MutableSet<Label>,
    var priority: Int,
) {
    val id: UUID = UUID.randomUUID()

    fun addTag(label: Label) {
        labels.add(label)
    }
}