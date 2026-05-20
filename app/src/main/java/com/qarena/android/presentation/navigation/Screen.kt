package com.qarena.android.presentation.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Subjects : Screen("subjects")
    data object Questions : Screen("questions/{subjectCode}") {
        fun createRoute(subjectCode: String): String {
            return "questions/$subjectCode"
        }
    }
}
