package services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream


object AuthService {
    private val json = Json { ignoreUnknownKeys = true }

    private lateinit var settings: AuthSettings
    private lateinit var client: HttpClient
    var idToken: String = ""

    fun init(client: HttpClient) {
        val configString = this::class.java.classLoader.getResourceAsStream("auth-settings.json")!!
        print(configString)
        settings = json.decodeFromStream(configString)

        this.client = client
        AuthenticationManager.init(client)
    }

    private var apiKey = "AIzaSyChADePJB6Ycal_6fSCPJGAqt23pv7oQWY"

    fun getEndpoint(action: String): String {
        return "https://identitytoolkit.googleapis.com/v1/${action}?key=${apiKey}"
    }

    suspend fun googleAuth() {
        val googleToken = AuthenticationManager.authenticateUser(
            authUrl = settings.google.authUri, // Config.domain,
            tokenUrl = settings.google.tokenUri,
            clientId = settings.google.clientId, // Config.clientId,
            clientSecret = settings.google.clientSecret,
            redirectUri = "http://localhost:5000/oauth-authorized/google",//"https://todo-app-53e07.firebaseapp.com/__/auth/handler",
            scope = "openid https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email", // https://www.googleapis.com/auth/firebase.database",
        )


        idToken = googleToken.idToken

//        // get firebase Token
//        val firebaseToken
//        val body = Parameters.build {
//            append("id_token", googleToken.idToken)
//            append("providerId", "google.com")
//        }.formUrlEncode()
    }

//    suspend fun firebaseSignInWithOAuth(postBody: String): FirebaseRet {
//        print("postBody:")
//        print(postBody)
//        val endpoint = getEndpoint("accounts:signInWithIdp")
//        val body = FirebaseRequest(
//            requestUri = "https://todo-app-53e07.firebaseapp.com/__/auth/handler",
//            postBody = postBody,
//            returnSecureToken = true,
//        )
//        val result = client.post(endpoint) {
//            contentType(ContentType.Application.Json)
//            setBody(body)
//        }
//        print("firebase res:")
//        print(result.body<String>())
//        return result.body()
//    }

    @Serializable
    class FirebaseRequest(
        val requestUri: String,
        val postBody: String,
        val returnSecureToken: Boolean = true,
        val returnIdpCredential: Boolean = true,
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
//        val rawUserInfo: String?,
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
        val google: GoogleSettings
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


suspend fun main() {
    val client = HttpClient() {
        install(ContentNegotiation) {
            json(Json{ ignoreUnknownKeys = true })
        }
        defaultRequest {
            url("http://127.0.0.1:8080")
        }
    }
    AuthService.init(client)
    AuthService.googleAuth()

    val c = HttpClient() {
        install(ContentNegotiation) {
            json(Json{ ignoreUnknownKeys = true })
        }
        defaultRequest {
            url("http://127.0.0.1:8080")
            bearerAuth(AuthService.idToken)
        }
    }
    BoardService.init(c)

    BoardService.getBoards()
}