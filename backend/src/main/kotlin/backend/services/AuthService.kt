package backend.services

import com.google.firebase.auth.FirebaseAuthException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import backend.models.AuthUser

class FirebaseAuthenticationProvider internal constructor(name: String?) : AuthenticationProvider(Configuration(name)) {
    class Configuration(name: String?) : Config(name)

    private val challengeFunction: ChallengeFunction = { _, call ->
        call.respond(HttpStatusCode.Unauthorized)
    }

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val token = context.call.request.authorization()?.split(" ")?.get(1)
        if (token == null) {
            context.challenge("firebase", AuthenticationFailedCause.NoCredentials, challengeFunction)
            return
        }
        try {
            val fbToken = FirebaseService.auth().verifyIdToken(token)
            print(fbToken.uid)
            context.principal(AuthUser(fbToken.uid))
        } catch (cause: FirebaseAuthException) {
            print("failed to verify")
            context.challenge("firebase", AuthenticationFailedCause.InvalidCredentials, challengeFunction)
        } catch (cause: Throwable) {
            print("failed to verify")
            context.challenge("firebase", AuthenticationFailedCause.InvalidCredentials, challengeFunction)
        }
    }
}
