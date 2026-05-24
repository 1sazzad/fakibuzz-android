package com.qarena.android.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.qarena.android.core.session.SessionManager
import com.qarena.android.core.session.TokenStorage
import com.qarena.android.core.session.hasAccessToken
import com.qarena.android.presentation.answers.AnswerScreen
import com.qarena.android.presentation.auth.EmailVerificationScreen
import com.qarena.android.presentation.auth.ForgotPasswordScreen
import com.qarena.android.presentation.auth.LoginScreen
import com.qarena.android.presentation.auth.RegisterScreen
import com.qarena.android.presentation.auth.ResetPasswordScreen
import com.qarena.android.presentation.feedback.FeedbackScreen
import com.qarena.android.presentation.home.HomeScreen
import com.qarena.android.presentation.common.AnswerPayload
import com.qarena.android.presentation.profile.ProfileScreen
import com.qarena.android.presentation.profile.ProfileSetupScreen
import com.qarena.android.presentation.questions.QuestionsScreen
import com.qarena.android.presentation.search.SearchScreen
import com.qarena.android.presentation.splash.SplashScreen
import com.qarena.android.presentation.subjects.SubjectAnalysisScreen
import com.qarena.android.presentation.subjects.SubjectOverviewScreen
import com.qarena.android.presentation.subjects.SubjectPredictionsScreen
import com.qarena.android.presentation.subjects.SubjectSuggestionsScreen
import com.qarena.android.presentation.subjects.SubjectsScreen
import com.google.gson.Gson

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenStorage = TokenStorage(context)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToProfileSetup = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Splash.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                onProfileCompleteLogin = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onProfileIncompleteLogin = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route) {
                        launchSingleTop = true
                    }
                },
                onForgotPasswordClick = {
                    navController.navigate(Screen.ForgotPassword.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.EmailVerification.route) {
                        launchSingleTop = true
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = Screen.EmailVerification.route) {
            EmailVerificationScreen(
                onVerificationSuccess = {
                    navController.navigate(Screen.Login.route) {
                        launchSingleTop = true
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackToLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        launchSingleTop = true
                    }
                },
                onResetPasswordClick = {
                    navController.navigate(Screen.ResetPassword.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onBackToLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = Screen.ProfileSetup.route) {
            ProtectedRoute(
                navController = navController,
                protectedRoute = Screen.ProfileSetup.route
            ) {
                ProfileSetupScreen(
                    onProfileSaved = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.ProfileSetup.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(route = Screen.Home.route) {
            ProtectedRoute(
                navController = navController,
                protectedRoute = Screen.Home.route
            ) {
                HomeScreen(
                    onViewSubjectsClick = {
                        navController.navigate(Screen.Subjects.route)
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    },
                    onFeedbackClick = {
                        navController.navigate(Screen.Feedback.route)
                    },
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onLogoutClick = {
                        SessionManager.clearSession(tokenStorage)
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(route = Screen.Profile.route) {
            ProtectedRoute(
                navController = navController,
                protectedRoute = Screen.Profile.route
            ) {
                ProfileScreen()
            }
        }

        composable(route = Screen.Search.route) {
            ProtectedRoute(
                navController = navController,
                protectedRoute = Screen.Search.route
            ) {
                SearchScreen(
                    onOpenSubjectOverviewClick = { subjectCode ->
                        navController.navigate(Screen.SubjectOverview.createRoute(subjectCode))
                    },
                    onGetAnswerClick = { answerPayload ->
                        saveAnswerPayload(
                            savedStateHandle = navController.currentBackStackEntry?.savedStateHandle,
                            answerPayload = answerPayload
                        )
                        navController.navigate(Screen.Answer.createRoute(answerPayload.subjectCode))
                    }
                )
            }
        }

        composable(route = Screen.Feedback.route) {
            ProtectedRoute(
                navController = navController,
                protectedRoute = Screen.Feedback.route
            ) {
                FeedbackScreen()
            }
        }

        composable(route = Screen.Subjects.route) {
            ProtectedRoute(
                navController = navController,
                protectedRoute = Screen.Subjects.route
            ) {
                SubjectsScreen(
                    onSubjectClick = { subjectCode ->
                        navController.navigate(Screen.SubjectOverview.createRoute(subjectCode))
                    }
                )
            }
        }

        composable(route = Screen.SubjectOverview.route) { navBackStackEntry ->
            val subjectCode = navBackStackEntry.arguments?.getString("subjectCode") ?: ""

            ProtectedRoute(
                navController = navController,
                protectedRoute = Screen.SubjectOverview.route
            ) {
                SubjectOverviewScreen(
                    subjectCode = subjectCode,
                    onOpenQuestionsClick = { selectedSubjectCode ->
                        navController.navigate(Screen.Questions.createRoute(selectedSubjectCode))
                    },
                    onOpenSuggestionsClick = { selectedSubjectCode ->
                        navController.navigate(Screen.Suggestions.createRoute(selectedSubjectCode))
                    },
                    onOpenAnalysisClick = { selectedSubjectCode ->
                        navController.navigate(Screen.Analysis.createRoute(selectedSubjectCode))
                    },
                    onOpenPredictionsClick = { selectedSubjectCode ->
                        navController.navigate(Screen.Predictions.createRoute(selectedSubjectCode))
                    },
                    onGetAnswerClick = { answerPayload ->
                        saveAnswerPayload(
                            savedStateHandle = navController.currentBackStackEntry?.savedStateHandle,
                            answerPayload = answerPayload
                        )
                        navController.navigate(Screen.Answer.createRoute(answerPayload.subjectCode.ifBlank { subjectCode }))
                    }
                )
            }
        }

        composable(route = Screen.Analysis.route) { navBackStackEntry ->
            val subjectCode = navBackStackEntry.arguments?.getString("subjectCode") ?: ""

            ProtectedRoute(
                navController = navController,
                protectedRoute = Screen.Analysis.route
            ) {
                SubjectAnalysisScreen(subjectCode = subjectCode)
            }
        }

        composable(route = Screen.Predictions.route) { navBackStackEntry ->
            val subjectCode = navBackStackEntry.arguments?.getString("subjectCode") ?: ""

            ProtectedRoute(
                navController = navController,
                protectedRoute = Screen.Predictions.route
            ) {
                SubjectPredictionsScreen(
                    subjectCode = subjectCode,
                    onGetAnswerClick = { answerPayload ->
                        saveAnswerPayload(
                            savedStateHandle = navController.currentBackStackEntry?.savedStateHandle,
                            answerPayload = answerPayload
                        )
                        navController.navigate(Screen.Answer.createRoute(answerPayload.subjectCode.ifBlank { subjectCode }))
                    }
                )
            }
        }

        composable(route = Screen.Suggestions.route) { navBackStackEntry ->
            val subjectCode = navBackStackEntry.arguments?.getString("subjectCode") ?: ""

            ProtectedRoute(
                navController = navController,
                protectedRoute = Screen.Suggestions.route
            ) {
                SubjectSuggestionsScreen(
                    subjectCode = subjectCode,
                    onGetAnswerClick = { answerPayload ->
                        saveAnswerPayload(
                            savedStateHandle = navController.currentBackStackEntry?.savedStateHandle,
                            answerPayload = answerPayload
                        )
                        navController.navigate(Screen.Answer.createRoute(answerPayload.subjectCode.ifBlank { subjectCode }))
                    }
                )
            }
        }

        composable(route = Screen.Questions.route) { navBackStackEntry ->
            val subjectCode = navBackStackEntry.arguments?.getString("subjectCode") ?: ""

            ProtectedRoute(
                navController = navController,
                protectedRoute = Screen.Questions.route
            ) {
                QuestionsScreen(
                    subjectCode = subjectCode,
                    onGetAnswerClick = { answerPayload ->
                        saveAnswerPayload(
                            savedStateHandle = navController.currentBackStackEntry?.savedStateHandle,
                            answerPayload = answerPayload
                        )
                        navController.navigate(Screen.Answer.createRoute(answerPayload.subjectCode.ifBlank { subjectCode }))
                    }
                )
            }
        }

        composable(route = Screen.Answer.route) { navBackStackEntry ->
            val subjectCode = navBackStackEntry.arguments?.getString("subjectCode") ?: ""
            val savedStateHandle = navController.previousBackStackEntry?.savedStateHandle
            val answerPayload = savedStateHandle?.toAnswerPayload(subjectCode)

            ProtectedRoute(
                navController = navController,
                protectedRoute = Screen.Answer.route
            ) {
                AnswerScreen(
                    subjectCode = subjectCode,
                    answerPayload = answerPayload ?: AnswerPayload(subjectCode = subjectCode)
                )
            }
        }
    }
}

@Composable
private fun ProtectedRoute(
    navController: NavHostController,
    protectedRoute: String,
    content: @Composable () -> Unit
) {
    if (hasAccessToken()) {
        content()
    } else {
        LaunchedEffect(protectedRoute) {
            navController.navigate(Screen.Login.route) {
                popUpTo(protectedRoute) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }
}

private const val ANSWER_PAYLOAD_KEY = "answer_payload"

private val answerPayloadGson = Gson()

private fun saveAnswerPayload(
    savedStateHandle: SavedStateHandle?,
    answerPayload: AnswerPayload
) {
    savedStateHandle?.set(ANSWER_PAYLOAD_KEY, answerPayloadGson.toJson(answerPayload))
}

private fun SavedStateHandle.toAnswerPayload(defaultSubjectCode: String): AnswerPayload? {
    val json = get<String>(ANSWER_PAYLOAD_KEY) ?: return null
    val payload = runCatching {
        answerPayloadGson.fromJson(json, AnswerPayload::class.java)
    }.getOrNull() ?: return null

    return payload.copy(
        subjectCode = payload.subjectCode.ifBlank { defaultSubjectCode }
    )
}
