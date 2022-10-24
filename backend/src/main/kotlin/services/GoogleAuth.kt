package services

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import models.User
import java.util.*

object GoogleAuthService {
    val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
        .setAudience(Collections.singletonList("101606477015-tdl4cnd6kqtqqu45cpiclen32f1g6835.apps.googleusercontent.com"))
        .build();

    fun verify(token: String): GoogleIdToken {
        return verifier.verify(token)
    }
}

class GoogleAuthProvider internal constructor(name: String?) : AuthenticationProvider(Configuration(name)) {
    class Configuration(name: String?) : Config(name)

    private val challengeFunction: ChallengeFunction = { _, call ->
        call.respond(HttpStatusCode.Unauthorized)
    }

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val token = context.call.request.authorization()?.split(" ")?.get(1)
        if (token == null) {
            context.challenge("google-Oauth2", AuthenticationFailedCause.NoCredentials, challengeFunction)
            return
        }
        try {
            val payload = GoogleAuthService.verify(token).payload
            print(payload.subject)
            context.principal(User(payload.subject))
        } catch (cause: Throwable) {
            print("failed to verify")
            context.challenge("google-Oauth2", AuthenticationFailedCause.InvalidCredentials, challengeFunction)
        }
    }
}

fun AuthenticationConfig.google(
    name: String? = null
) {
    val provider = GoogleAuthProvider(name)
    register(provider)
}
