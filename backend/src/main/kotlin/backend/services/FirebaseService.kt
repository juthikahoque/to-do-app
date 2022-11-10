package backend.services

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import java.io.InputStream

object FirebaseService {
    private val serviceAccount: InputStream? =
        this::class.java.classLoader.getResourceAsStream("todo-app-firebase-adminsdk.json")

    private val options: FirebaseOptions = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    init {
        FirebaseApp.initializeApp(options)
    }

    fun auth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}
