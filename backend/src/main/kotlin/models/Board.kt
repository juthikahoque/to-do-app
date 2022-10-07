package models

import java.util.UUID
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class Board(
    val name: String
) {
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID()
}


object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
            return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
            encoder.encodeString(value.toString())
    }
}
