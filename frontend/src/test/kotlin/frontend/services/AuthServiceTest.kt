package frontend.services

import org.junit.jupiter.api.Assertions.assertNotNull

// test google auth, then refresh token
suspend fun main() {
    AuthService.googleAuth()

    assertNotNull(AuthService.user)

    AuthService.refresh()
}
