package com.qarena.android.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.qarena.android.presentation.auth.LoginScreen
import com.qarena.android.presentation.home.HomeScreen
import com.qarena.android.presentation.questions.QuestionsScreen
import com.qarena.android.presentation.splash.SplashScreen
import com.qarena.android.presentation.subjects.SubjectsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onGetStartedClick = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(Screen.Home.route)
                }
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                onViewSubjectsClick = {
                    navController.navigate(Screen.Subjects.route)
                }
            )
        }

        composable(route = Screen.Subjects.route) {
            SubjectsScreen(
                onSubjectClick = { subjectCode ->
                    navController.navigate(Screen.Questions.createRoute(subjectCode))
                }
            )
        }

        composable(route = Screen.Questions.route) { navBackStackEntry ->
            val subjectCode = navBackStackEntry.arguments?.getString("subjectCode") ?: ""

            QuestionsScreen(
                subjectCode = subjectCode
            )
        }
    }
}
