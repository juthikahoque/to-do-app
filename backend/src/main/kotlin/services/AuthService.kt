package services

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseToken
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import models.User
import java.io.InputStream
import java.util.*

object FirebaseAuthService {
    private val serviceAccount: InputStream? =
        this::class.java.classLoader.getResourceAsStream("todo-app-firebase-adminsdk.json")

    private val options: FirebaseOptions = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    fun init(): FirebaseApp = FirebaseApp.initializeApp(options)

    fun verify(token: String): FirebaseToken? {
        return FirebaseAuth.getInstance().verifyIdToken(token)
    }
}

class FirebaseAuthenticationProvider internal constructor(name: String) : AuthenticationProvider(Configuration(name)) {
    class Configuration(name: String) : Config(name)

    private val challengeFunction: ChallengeFunction = { _, call ->
        call.respond(HttpStatusCode.Unauthorized)
    }

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val token = context.call.request.authorization()?.split(" ")?.get(1)
        if (token == null) {
            context.challenge("firebase", AuthenticationFailedCause.NoCredentials, challengeFunction)
            return
        }
//        print(token)
        try {
            val fbToken = FirebaseAuthService.verify(token)!!
            print(fbToken.uid)
            context.principal(User(fbToken.uid))
//            context.principal(User(UUID.fromString(token)))
        } catch (fdae: FirebaseAuthException) {
            print(fdae)
        } catch (cause: Throwable) {
            print("failed to verify")
            context.challenge("firebase", AuthenticationFailedCause.InvalidCredentials, challengeFunction)
        }
    }
}

fun AuthenticationConfig.firebase(
    name: String? = null
) {
    val provider = FirebaseAuthenticationProvider("firebase")
    register(provider)
}
