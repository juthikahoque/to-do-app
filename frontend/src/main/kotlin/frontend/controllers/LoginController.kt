package frontend.controllers

import frontend.app
import frontend.services.AuthService
import frontend.services.BoardService
import frontend.services.ItemService
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import javafx.animation.PauseTransition
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.util.Duration
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import kotlin.coroutines.CoroutineContext


class LoginController : CoroutineScope {

    var authJob: Job? = null

    override val coroutineContext: CoroutineContext = Dispatchers.JavaFx

    @FXML
    lateinit var googleSignInButton: Button

    @FXML
    lateinit var cancelLogin: Button

    @FXML
    lateinit var serverStatus: Button

    @FXML
    lateinit var serverUrl: TextField

    private val statusChecker = PauseTransition(Duration.seconds(1.0))

    @FXML
    fun initialize() {
        serverUrl.textProperty().addListener { _, _, _ ->
            statusChecker.playFromStart()
        }
        statusChecker.onFinished = EventHandler { _: ActionEvent? ->
            launch {
                serverStatus.isDisable = AuthService.serverStatusCheck(serverUrl.text)
                login()
            }
        }
        checkServerStatus()

        googleSignInButton.isDisable = false
        cancelLogin.isVisible = false

        login()
    }

    @FXML
    private fun onLoginWithGoogle() {
        googleSignInButton.isDisable = true
        cancelLogin.isVisible = true


        authJob = launch {
            try {
                AuthService.googleAuth()
            } catch (e: Throwable) {
                if (e.message == "unauthorized") {
                    print("unauthorized")
                    cancelLogin()
                    yield()
                }
            }
            login()
        }
    }

    private fun login() {
        if (!serverStatus.isDisable) return
//        if (AuthService.user == null) return
        if (AuthService.token == null) return
        println("logged in with good server")

        val token = AuthService.token!!
        val client = HttpClient {
            expectSuccess = true
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(token.idToken, token.refreshToken)
                    }
                    refreshTokens {
                        runBlocking {
                            AuthService.refresh()
                        }
                        BearerTokens(token.idToken, token.refreshToken)
                    }
                }
            }
            install(ContentNegotiation) {
                json()
            }
            defaultRequest {
                url(serverUrl.text)
            }
        }

        BoardService.init(client)
        ItemService.init(client)

        // then bring up home base
        app.switchToMain()
        // app.changeScene("/views/main-view.fxml")
    }

    @FXML
    private fun cancelLogin() {
        authJob?.cancel()

        googleSignInButton.isDisable = false
        cancelLogin.isVisible = false
    }

    @FXML
    private fun checkServerStatus() {
        statusChecker.playFrom(Duration.seconds(1.0))
    }

}
