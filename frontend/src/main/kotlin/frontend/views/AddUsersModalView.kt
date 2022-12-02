package frontend.views

import frontend.Model
import frontend.services.AuthService
import frontend.services.BoardService
import frontend.services.UserService
import frontend.utils.Actions
import frontend.utils.UndoRedoManager
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import kotlinx.coroutines.runBlocking
import models.User

class AddUsersModalView(private val model: Model) : BorderPane() {
    private val listOfUsers = model.currentBoard.value.users.toMutableSet()

    private val header = Label("Add Users:").apply {
        padding = Insets(0.0, 0.0, 10.0, 0.0)
        font = Font(18.0)
    }

    private val errorMessage = Label("").apply {
        padding = Insets(5.0, 0.0, 0.0, 0.0)
        textFill = Color.RED
        font = Font(10.0)
    }

    private val usersToAddLabel = Label("Users with access:").apply {
        padding = Insets(10.0, 0.0, 5.0, 0.0)
        font = Font(14.0)
    }

    private val nameInput = TextField().apply {
        promptText = "User Email..."
        prefWidth = 200.0
    }

    private val addButton = Button("Add").apply {
        setDisabled(nameInput.text.isEmpty())
        prefWidth = 60.0
        setOnAction {
            errorMessage.text = ""
            lateinit var users: List<User>
            runBlocking {
                users = UserService.getUserByEmail(nameInput.text)
            }

            if (users.isEmpty()) {
                errorMessage.text = "An invalid email was provided."
            } else if (listOfUsers.contains(users[0])) {
                errorMessage.text = "This user already has access to this board."
            } else {
                errorMessage.text = ""
                listOfUsers.add(users[0])
            }

            updateView()
        }
        isDefaultButton = true
    }

    private val nameInputHBox = HBox(nameInput, addButton)

    private val addAndListUsersVbox = VBox().apply {
        prefHeight = 150.0
        prefWidth = 260.0
    }

    private val confirmButton = Button("Confirm").apply {
        id = "save"
        background = Background(BackgroundFill(Color.LIGHTGREEN, CornerRadii(2.5), null))
        setOnAction {
            UndoRedoManager.handleAction(Actions.updateBoard, model.items, model.boards, null)

            val board = model.currentBoard.value.copy(users = listOfUsers)
            val idx = model.boards.indexOf(model.currentBoard.value)
            model.boards[idx] = board

            runBlocking {
                BoardService.updateBoard(board)
            }
            model.additionalModalView.set("")
        }
    }

    private val cancelButton = Button("Cancel").apply {
        id = "cancel"
        background = Background(BackgroundFill(Color.INDIANRED, CornerRadii(2.5), null))
        setOnAction {
            model.additionalModalView.set("")
        }
        isCancelButton = true
    }

    private val spacer = Pane().apply {
        HBox.setHgrow(this, Priority.ALWAYS)
    }

    private val buttons = HBox(spacer, cancelButton, confirmButton).apply {
        spacing = 10.0
    }

    private fun updateView() {
        addAndListUsersVbox.children.clear()
        addAndListUsersVbox.children.add(nameInputHBox)
        addAndListUsersVbox.children.add(errorMessage)
        addAndListUsersVbox.children.add(usersToAddLabel)
        for (user in listOfUsers) {
            addAndListUsersVbox.children.add(
                HBox( // user display row
                    Label("${user.name} (${user.email})"),
                    Button().apply {
                        isVisible = user != AuthService.user
                        id = "x-button"
                        padding = Insets(0.0)
                        setOnAction {
                            listOfUsers.remove(user)
                            updateView()
                        }
                    }
                )
            )
        }
        center = addAndListUsersVbox
    }

    init {
        id = "modal"
        padding = Insets(20.0)
        maxWidth = 300.0
        maxHeight = 300.0
        background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))
        top = header
        bottom = buttons
        updateView()

        Platform.runLater { nameInput.requestFocus() }
    }
}
