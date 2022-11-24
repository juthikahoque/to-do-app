package models

class AppError(message: String, val type: String) : RuntimeException(message) {
    companion object {
        const val NotFound = "NotFound"
        const val Unexpected = "Unexpected"
        const val BadRequest = "BadRequest"
    }
}

fun appError(message: String, type: String = AppError.Unexpected): Nothing = throw AppError(message, type)