package models

import io.ktor.server.auth.*
import java.util.*

data class User(
    val id: UUID,
): Principal