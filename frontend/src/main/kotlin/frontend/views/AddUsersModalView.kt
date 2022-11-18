package frontend.views

import frontend.Model
import frontend.services.BoardService
import frontend.services.UserService
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import kotlinx.coroutines.runBlocking
import models.User

class AddUsersModalView(private val model: Model): BorderPane() {
    private val listOfUsers = mutableSetOf<User>()

    private val header = Label("Add Users:").apply {
        padding = Insets(0.0, 0.0, 10.0, 0.0)
        font = Font(18.0)
    }

    private val errorMessage = Label("").apply {
        padding = Insets(5.0, 0.0, 0.0, 0.0)
        textFill = Color.RED
        font = Font(10.0)
    }

    private val usersToAddLabel = Label("Users To Add:").apply {
        padding = Insets(10.0, 0.0, 5.0, 0.0)
        font = Font(14.0)
    }

    private val nameInput = TextField().apply {
        promptText = "User Email..."
        prefWidth = 200.0

        setOnAction {
            addUser()
        }
    }

    private fun addUser() {
        lateinit var users: List<User>
        runBlocking {
            users = UserService.getUserByEmail(nameInput.text)
        }

        if (users.isEmpty()) {
            errorMessage.text = "An invalid email was provided."
        } else if (model.getCurrentBoard().users.contains(users[0].userId)) {
            errorMessage.text = "This user already has access to this board."
        } else if (listOfUsers.contains(users[0])) {
            errorMessage.text = "This user is already set to be added."
        }  else {
            errorMessage.text = ""
            listOfUsers.add(users[0])
        }

        updateView()
    }

    private val addButton = Button("Add").apply {
        setDisabled(nameInput.text.isEmpty())
        prefWidth = 60.0
        setOnAction {
            addUser()
        }
    }

    private val nameInputHbox = HBox(nameInput, addButton)

    private val addAndListUsersVbox = VBox().apply {
        prefHeight = 150.0
        prefWidth = 260.0
    }

    private val confirmButton = Button("Confirm").apply{
        background = Background(BackgroundFill(Color.LIGHTGREEN, CornerRadii(2.5), null))

        setOnAction {
            for (user in listOfUsers) {
                model.getCurrentBoard().users.add(user.userId)
            }

            runBlocking {
                BoardService.updateBoard(model.getCurrentBoard())
            }

            model.setShowAddUserModal(false)
        }
    }

    private val cancelButton = Button("Cancel").apply{
        background = Background(BackgroundFill(Color.INDIANRED, CornerRadii(2.5), null))
        setOnAction {
            model.setShowAddUserModal(false)
        }
    }

    private val buttons = HBox(confirmButton, cancelButton).apply{
        spacing = 10.0
    }

    private fun updateView() {

    }

    init {
        padding = Insets(20.0)
        maxWidth = 300.0
        maxHeight = 300.0
        background = Background(BackgroundFill(Color.WHITE, CornerRadii(5.0), Insets(0.0)))

        addAndListUsersVbox.children.clear()
        addAndListUsersVbox.children.add(nameInputHbox)
        addAndListUsersVbox.children.add(errorMessage)
        addAndListUsersVbox.children.add(usersToAddLabel)
        for (user in listOfUsers) {
            addAndListUsersVbox.children.add(Label(user.name))
        }
        top = header
        center = addAndListUsersVbox
        bottom = buttons
        updateView()
    }
}