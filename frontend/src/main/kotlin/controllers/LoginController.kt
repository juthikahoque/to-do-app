package controller

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.*
import javafx.scene.layout.*
import javafx.stage.Stage
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import javafx.fxml.FXMLLoader
import javafx.event.ActionEvent
import javafx.application.Platform
import javafx.beans.binding.*
import javafx.beans.property.*
import javafx.beans.value.*

import kotlinx.coroutines.*

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import services.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*

import app
import javafx.scene.control.Button

class LoginController {
    init {
        AuthService.init()

        if (AuthService.token != null) {
            loggedIn()
        }
    }

    var authJob: Job? = null

    @FXML
    lateinit var googleSignInButton: Button

    @FXML
    lateinit var cancelLogin: Button

    @FXML
    private fun onLoginWithGoogle() {
        googleSignInButton.isDisable = true
        cancelLogin.isVisible = true

        authJob = GlobalScope.launch {
            try {
                AuthService.googleAuth()
            } catch (e: Throwable) {
                if (e.message == "unauthorized") {
                    print("unauthorized")
                    cancelLogin()
                    yield()
                }
            }
            loggedIn()            
        }
    }

    private fun loggedIn() {
        val token = AuthService.token!!
        val client = HttpClient() {
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
                url("http://127.0.0.1:8080")
            }
        }

        BoardService.init(client)
        ItemService.init(client)

        // then bring up home base
        Platform.runLater { app.switchToMain() }
        // app.changeScene("/views/main-view.fxml")
    }

    @FXML
    private fun cancelLogin() {
        authJob?.cancel()

        googleSignInButton.isDisable = false
        cancelLogin.isVisible = false
    }
}
