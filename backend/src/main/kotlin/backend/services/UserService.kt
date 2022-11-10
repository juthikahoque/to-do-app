package services

import backend.services.FirebaseService
import com.google.firebase.auth.UserRecord
import models.User

object UserService {

    fun getUserFromRecord(record: UserRecord): User {
        return User(
            email = record.email,
            userId = record.uid,
            name = record.displayName,
        )
    }

    fun getUserByEmail(email: String): User {
        val userRecord = FirebaseService.auth().getUserByEmail(email)
        return getUserFromRecord(userRecord)
    }

    fun getAllUsersByEmails(emails: List<String>): List<User> {
        val res = mutableListOf<User>()
        for (email in emails) {
            res.add(getUserByEmail(email))
        }
        return res
    }

    fun getUserById(id: String): User {
        val userRecord = FirebaseService.auth().getUser(id)
        return getUserFromRecord(userRecord)
    }
}