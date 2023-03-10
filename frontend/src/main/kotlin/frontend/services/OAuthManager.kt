package frontend.services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.awt.Desktop
import java.net.URI
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

object OAuthManager {

    private lateinit var client: HttpClient

    fun init(httpClient: HttpClient) {
        client = httpClient
    }

    suspend fun authenticateUser(
        authUrl: String,
        tokenUrl: String,
        clientId: String,
        clientSecret: String,
        redirectUri: String,
        scope: String,
    ): TokenResponse {
        // val job = coroutineScope.launch {
        var retval: TokenResponse? = null

        try {
            val verifier = createVerifier()
            val challenge = createChallenge(verifier)
            val state = createState()

            // pop up auth window
            val urlParam = Parameters.build {
                append("client_id", clientId)
                append("redirect_uri", redirectUri)
                append("response_type", "code")
                append("scope", scope)
                append("code_challenge", challenge)
                append("code_challenge_method", "S256")
                append("state", state)
                append("access_type", "offline")
                append("prompt", "consent")
            }.formUrlEncode()

            // println("Launching URL: $authUrl?$urlParam")

            withContext(Dispatchers.IO) {
                Desktop.getDesktop().browse(URI("$authUrl?$urlParam"))
            }

            val code = waitForCallback(state)

            // get token
            val response = client.post(tokenUrl) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(FormDataContent(Parameters.build {
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                    append("code", code)
                    append("code_verifier", verifier)
                    append("grant_type", "authorization_code")
                    append("redirect_uri", redirectUri)
                    // append("audience", audience)
                }))
            }
            println(response.body<String>())
            retval = response.body()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (retval == null)
            error("unauthorized")
        else
            return retval
    }

    private suspend fun waitForCallback(state: String): String {
        var server: NettyApplicationEngine? = null
        try {
            val code = suspendCancellableCoroutine<String> { continuation ->
                server = embeddedServer(Netty, port = 5000) {
                    routing {
                        get("/oauth-authorized/google") {
                            val code = call.parameters["code"] ?: throw RuntimeException("Received a response with no code")
                            if (call.parameters["state"] != state) {
                                throw RuntimeException("Received invalid state")
                            }
                            call.respond("""Sign in successful""")
                            println(code)
                            continuation.resume(code)
                        }
                    }
                }.start(wait = false)
            }
        // coroutineScope.launch {
            server!!.stop(0, 0, TimeUnit.SECONDS)
            // }

            return code
        } catch (e: CancellationException){
            println("Work cancelled!")
            server?.stop(0, 0, TimeUnit.SECONDS)
            throw e
        }
    }

    private fun createVerifier(): String {
        val sr = SecureRandom()

        val code = ByteArray(32)
        sr.nextBytes(code)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(code)
    }

    private fun createChallenge(verifier: String): String {
        val bytes: ByteArray = verifier.toByteArray(Charsets.US_ASCII)
        val md = MessageDigest.getInstance("SHA-256")
        md.update(bytes, 0, bytes.size)
        val digest = md.digest()
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    private fun createState(): String {
        val sr = SecureRandom()

        val code = ByteArray(32)
        sr.nextBytes(code)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(code)
    }

    @Serializable
    data class TokenResponse(
        @SerialName("access_token")
        val accessToken: String,
        @SerialName("refresh_token")
        val refreshToken: String,
        @SerialName("id_token")
        val idToken: String,
        val scope: String,
        @SerialName("expires_in")
        val expiresIn: Int,
        @SerialName("token_type")
        val tokenType: String,
    )
}


