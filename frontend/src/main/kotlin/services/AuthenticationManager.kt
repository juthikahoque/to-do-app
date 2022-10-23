
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.apache.commons.codec.binary.Base64
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

object AuthenticationManager {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val callbackJob = MutableStateFlow<Job?>(null)

    val isLoggingIn = callbackJob.map { it?.isActive == true }

    private lateinit var client: HttpClient;

    fun init(httpClient: HttpClient) {
        client = httpClient;
    }

    fun authenticateUser(
        authUrl: String,
        tokenUrl: String,
        clientId: String,
        clientSecret: String,
        redirectUri: String,
        scope: String,
    ) {
        // val job = coroutineScope.launch {
        runBlocking {
            try {
                val verifier = createVerifier()
                val challenge = createChallenge(verifier)
                val url = createLoginUrl(
                    domain = authUrl,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    scope = scope,
                    challenge = challenge,
                )

                println("Launching URL: $url")

                withContext(Dispatchers.IO) {
                    Desktop.getDesktop().browse(URI(url))
                }

                val code = waitForCallback()

                return@runBlocking getToken(
                    domain = tokenUrl,
                    clientId = clientId,
                    verifier = verifier,
                    code = code,
                    clientSecret = clientSecret,
                    redirectUri = redirectUri,
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

//        callbackJob.value = job
//        job.invokeOnCompletion { callbackJob.value = null }
//
//        runBlocking {
//            job
//        }
    }

    private suspend fun getToken(
        domain: String,
        clientId: String,
        verifier: String,
        clientSecret: String,
        code: String,
        redirectUri: String,
    ): TokenResponse {
        val response = client.post(domain) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(FormDataContent(Parameters.build {
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("code", code)
                append("code_verifier", verifier)
                append("grant_type", "authorization_code")
                append("redirect_uri", redirectUri)
            }))
        }

        println("response: ${response.body<String>()}")

        return response.body()
    }

    private suspend fun waitForCallback(): String {
        var server: NettyApplicationEngine? = null

        val code = suspendCancellableCoroutine<String> { continuation ->
            server = embeddedServer(Netty, port = 5000) {
                routing {
                    get ("/oauth-authorized/google") {
                        val code = call.parameters["code"] ?: throw RuntimeException("Received a response with no code")
                        println("got code: $code")
                        call.respondText("OK")

                        continuation.resume(code)
                    }
                }
            }.start(wait = false)
        }

        coroutineScope.launch {
            server!!.stop(1, 5, TimeUnit.SECONDS)
        }

        return code
    }

    private fun createLoginUrl(
        domain: String,
        clientId: String,
        redirectUri: String,
        scope: String,
        challenge: String,
    ): String {
        val encodedRedirectUri = URLEncoder.encode(redirectUri, Charsets.UTF_8)
        val encodedScope = URLEncoder.encode(scope, Charsets.UTF_8)

        return "$domain?client_id=$clientId&redirect_uri=$encodedRedirectUri&response_type=code&scope=$encodedScope&access_type=offline&code_challenge=$challenge&code_challenge_method=S256"
    }

    private fun createVerifier(): String {
        val sr = SecureRandom()
        val code = ByteArray(32)
        sr.nextBytes(code)
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(code)
    }

    private fun createChallenge(verifier: String): String {
        val bytes: ByteArray = verifier.toByteArray(Charsets.US_ASCII)
        val md = MessageDigest.getInstance("SHA-256")
        md.update(bytes, 0, bytes.size)
        val digest = md.digest()
        return Base64.encodeBase64URLSafeString(digest)
    }

    fun cancelLogin() {
        callbackJob.value?.cancel()
        callbackJob.value = null
    }
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

object AuthService {
    private val settings: AuthSettings = Json.decodeFromStream(this::class.java.classLoader.getResource("auth-settings.json").openStream())

    lateinit var client: HttpClient

    fun init(client: HttpClient) {
        this.client = client
        AuthenticationManager.init(client)
    }

    class AuthRequst(
        val postBody: String = "id_token=[token]&providerId=google.com",
        val requestUri: String = "http://localhost",
        val returnIdpCredential: Boolean = true,
        val returnSecureToken: Boolean = true,
    )

    private var apiKey = ""

    fun getEndpoint(action: String): String {
        return "https://identitytoolkit.googleapis.com/v1/${action}?key=${apiKey}"
    }

    fun googleAuth() {
        val googleToken = AuthenticationManager.authenticateUser(
            authUrl = settings.google.auth_url, // Config.domain,
            tokenUrl = settings.google.token_url,
            clientId = settings.google.clientId, // Config.clientId,
            clientSecret = settings.google.client_secret,
            redirectUri = "http://localhost:5000/oauth-authorized/google",
            scope = "openid",
        )
        getEndpoint("")

//        val firebaseToken
    }

    fun firebaseSignInWithOAuth() {
        getEndpoint("accounts:signInWithIdp")
    }

//    class FirebaseRet {
//        federatedId	string	The unique ID identifies the IdP account.
//        providerId	string	The linked provider ID (e.g. "google.com" for the Google provider).
//        localId	string	The uid of the authenticated user.
//        emailVerified	boolean	Whether the sign-in email is verified.
//        email	string	The email of the account.
//        oauthIdToken	string	The OIDC id token if available.
//        oauthAccessToken	string	The OAuth access token if available.
//        oauthTokenSecret	string	The OAuth 1.0 token secret if available.
//        rawUserInfo	string	The stringified JSON response containing all the IdP data corresponding to the provided OAuth credential.
//        firstName	string	The first name for the account.
//        lastName	string	The last name for the account.
//        fullName	string	The full name for the account.
//        displayName	string	The display name for the account.
//        photoUrl	string	The photo Url for the account.
//        idToken	string	A Firebase Auth ID token for the authenticated user.
//        refreshToken	string	A Firebase Auth refresh token for the authenticated user.
//        expiresIn	string	The number of seconds in which the ID token expires.
//        needConfirmation	boolean
//    }

    class AuthSettings (
        val google: GoogleSettings
    ) {
        class GoogleSettings(
            @SerialName("client_id")
            val clientId: String,
            @SerialName("auth_url")
            val auth_url: String,
            @SerialName("token_url")
            val token_url: String,
            @SerialName("client_secret")
            val client_secret: String,
        )
    }
}



fun main() {
    val client = HttpClient() {
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            url("http://127.0.0.1:8080")
        }
    }
}

