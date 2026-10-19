package ru.mindlex.ui.navigation

/** Маршруты навигации приложения. */
object Routes {

    const val AUTH = "auth"
    const val DASHBOARD = "dashboard"
    const val WORDS = "words"
    const val SETTINGS = "settings"
    const val PRACTICE = "practice/{mechanic}"

    const val ARG_MECHANIC = "mechanic"

    fun practice(mechanic: String): String = "practice/$mechanic"
}
