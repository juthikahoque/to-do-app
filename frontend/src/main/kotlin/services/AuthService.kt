package services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.decodeFromStream
import java.lang.RuntimeException

object AuthService {
    private val json = Json { ignoreUnknownKeys = true }

    private lateinit var settings: AuthSettings
    private lateinit var client: HttpClient
    lateinit var user: FirebaseRet
    private var token: Token? = null

    fun init() {
        val configString = this::class.java.classLoader.getResourceAsStream("auth-settings.json")!!
        print(configString)
        settings = json.decodeFromStream(configString)

        client = HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
        }
        AuthenticationManager.init(client)
    }

    private fun endpoint(action: String): String {
        return "https://identitytoolkit.googleapis.com/v1/${action}?key=${settings.firebaseApiKey}"
    }

    suspend fun googleAuth() {
        val googleToken = AuthenticationManager.authenticateUser(
            authUrl = settings.google.authUri, // Config.domain,
            tokenUrl = settings.google.tokenUri,
            clientId = settings.google.clientId, // Config.clientId,
            clientSecret = settings.google.clientSecret,
            redirectUri = "http://localhost:5000/oauth-authorized/google",
            scope = "openid https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email",
        )
        // get firebase Token
        val body = Parameters.build {
            append("id_token", googleToken.idToken)
            append("providerId", "google.com")
        }.formUrlEncode()
        user = firebaseSignInWithOAuth(body)
    }

    suspend fun refresh() {
        val token = token ?: throw RuntimeException("User not logged in")
        val result = client.post(endpoint("token")) {
            contentType(ContentType.Application.Json)
            setBody("""{"refresh_token":"${token.refreshToken}","grant_type":"refresh_token"}""")
        }
        this.token = result.body()
    }

    private suspend fun firebaseSignInWithOAuth(postBody: String): FirebaseRet {
        val result = client.post(endpoint("accounts:signInWithIdp")) {
            contentType(ContentType.Application.Json)
            setBody("""{"postBody":"$postBody","requestUri":"http://localhost","returnSecureToken":true}""")
        }
        token = result.body()
        return result.body()
    }

    @Serializable
    class Token(
        @JsonNames("expires_in")
        val expiresIn: String,
        @JsonNames("refresh_token")
        val refreshToken: String,
        @JsonNames("id_token")
        val idToken: String,
        @JsonNames("localId", "user_id")
        val userId: String,
    )

    @Serializable
    class FirebaseRet(
        val federatedId: String = "",
        val providerId: String = "",
        val localId: String = "",
        val emailVerified: Boolean = false,
        val email: String = "",
        val oauthIdToken: String = "",
        val oauthAccessToken: String = "",
        val oauthTokenSecret: String = "",
        val rawUserInfo: String? = "",
        val firstName: String = "",
        val lastName: String = "",
        val fullName: String = "",
        val displayName: String = "",
        val photoUrl: String = "",
        val idToken: String = "",
        val refreshToken: String = "",
        val expiresIn: String = "",
        val needConfirmation: Boolean = false,
    )

    @Serializable
    class AuthSettings(
        val google: GoogleSettings,
        val firebaseApiKey: String
    ) {
        @Serializable
        class GoogleSettings(
            @SerialName("client_id")
            val clientId: String,
            @SerialName("auth_uri")
            val authUri: String,
            @SerialName("token_uri")
            val tokenUri: String,
            @SerialName("client_secret")
            val clientSecret: String,
        )
    }
}
