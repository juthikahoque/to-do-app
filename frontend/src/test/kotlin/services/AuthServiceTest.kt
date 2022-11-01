package services

import org.junit.jupiter.api.Assertions.assertNotNull

// test google auth, then refresh token
suspend fun main() {
    AuthService.init()

    AuthService.googleAuth()

    assertNotNull(AuthService.user)

    AuthService.refresh()
}