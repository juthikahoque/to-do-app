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

import kotlinx.coroutines.*

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import services.*
import app

class LoginController {
    init {
        AuthService.init()
    }

    @FXML
    private fun onLoginWithGoogle() {
        runBlocking {
            AuthService.googleAuth()

            val client = HttpClient() {
                // install(Auth) {

                // }
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
            app.switchToMain()
            // app.changeScene("/views/main-view.fxml")
        }
    }
}
