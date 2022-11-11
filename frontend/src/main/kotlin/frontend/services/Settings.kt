package frontend.services

import java.util.prefs.Preferences

val Settings: Preferences = Preferences.userRoot().node("cs346-todo")
