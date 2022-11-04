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

class LoginController {
    init {
        AuthService.init()
    }

    var authJob: Job? = null

    var loggingIn: SimpleBooleanProperty = SimpleBooleanProperty(false)
    fun loggingInProperty(): BooleanProperty = loggingIn
    fun getLoggingIn(): Boolean = loggingIn.get()
    fun setloggingIn(value: Boolean) { return loggingIn.set(value)}

    @FXML
    private fun onLoginWithGoogle() {
        print(loggingIn.get())
        loggingIn.setValue(true)
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

            print("logged in ")
            val client = HttpClient {
                expectSuccess = true
                install(Auth) {
                    bearer {
                        loadTokens {
                            BearerTokens(AuthService.token!!.idToken, AuthService.token!!.refreshToken)
                        }
                        refreshTokens {
                            runBlocking {
                                AuthService.refresh()
                            }
                            BearerTokens(AuthService.token!!.idToken, AuthService.token!!.refreshToken)
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
            loggingIn.setValue(false)
        }
    }

    @FXML
    private fun cancelLogin() {
        authJob?.cancel()
        loggingIn.setValue(false)
    }
}
