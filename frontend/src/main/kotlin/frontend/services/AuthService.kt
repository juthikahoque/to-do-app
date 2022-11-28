package frontend.services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.decodeFromStream
import models.User

@OptIn(ExperimentalSerializationApi::class)
object AuthService {
    private val json = Json { ignoreUnknownKeys = true }

    private var settings: AuthSettings = json.decodeFromStream(
        this::class.java.classLoader.getResourceAsStream("auth-settings.json")!!
    )
    private var client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    val user: User
        get() = firebaseUser!!.let {
            User(
                it.localId,
                it.displayName,
                it.email,
            )
        }
    var firebaseUser: FirebaseRet? = null
    var token: Token? = null

    init {
        OAuthManager.init(client)

        val tokenString = Settings.get("auth.token", "")
        if (tokenString != "") {
            token = json.decodeFromString<Token>(tokenString)
            runBlocking {
                refresh() // refresh the token to avoid expired token on first login
                firebaseUser = getUserInfo()
            }
        }
    }

    private fun endpoint(action: String): String {
        return "https://identitytoolkit.googleapis.com/v1/${action}?key=${settings.firebaseApiKey}"
    }

    suspend fun googleAuth() {
        val googleToken = OAuthManager.authenticateUser(
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

        firebaseUser = firebaseSignInWithOAuth(body)
    }

    suspend fun refresh() {
        val token = token ?: throw RuntimeException("User not logged in")
        val result = client.post(endpoint("token")) {
            contentType(ContentType.Application.Json)
            setBody("""{"refresh_token":"${token.refreshToken}","grant_type":"refresh_token"}""")
        }
        this.token = result.body()

        Settings.put("auth.token", json.encodeToString(this.token))
    }

    private suspend fun firebaseSignInWithOAuth(postBody: String): FirebaseRet {
        val result = client.post(endpoint("accounts:signInWithIdp")) {
            contentType(ContentType.Application.Json)
            setBody("""{"postBody":"$postBody","requestUri":"http://localhost","returnSecureToken":true}""")
        }
        this.token = result.body()

        Settings.put("auth.token", json.encodeToString(this.token))

        return result.body()
    }

    private suspend fun getUserInfo(): FirebaseRet {
        val result = client.post(endpoint("accounts:lookup")) {
            contentType(ContentType.Application.Json)
            setBody("""{"idToken":"${token!!.idToken}"}""")
        }
        println(result.body<String>())
        return result.body<AccountInfoResponse>().users.first()
    }

    fun logout() {
        Settings.put("auth.token", "")
        this.token = null
        this.firebaseUser = null
    }

    suspend fun serverStatusCheck(url: String): Boolean {
        try {
            val result = client.get("$url/health")
            return result.status == HttpStatusCode.OK
        } catch (ex: Exception) {
            return false
        }
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
    class AccountInfoResponse(
        val kind: String,
        val users: List<FirebaseRet>,
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
