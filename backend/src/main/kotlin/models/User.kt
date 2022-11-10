package models

import io.ktor.server.auth.*

data class AuthUser(
    val id: String,
): Principal