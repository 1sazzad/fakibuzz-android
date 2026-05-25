package com.qarena.android.presentation.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object EmailVerification : Screen("verify-email")
    data object ForgotPassword : Screen("forgot-password")
    data object ResetPassword : Screen("reset-password")
    data object ProfileSetup : Screen("profile-setup")
    data object Profile : Screen("profile")
    data object Home : Screen("home")
    data object Feedback : Screen("feedback")
    data object Search : Screen("search")
    data object Subjects : Screen("subjects")
    data object SubjectOverview : Screen("subject-overview/{subjectCode}") {
        fun createRoute(subjectCode: String): String {
            return "subject-overview/${Uri.encode(subjectCode)}"
        }
    }
    data object SimilarQuestions : Screen("similar-questions/{subjectCode}?subjectName={subjectName}&academicLevel={academicLevel}&group={group}&paperType={paperType}") {
        fun createRoute(args: SelectedSubjectNavArgs): String {
            return args.createRoute("similar-questions")
        }
    }
    data object TopicAnalysis : Screen("topic-analysis/{subjectCode}?subjectName={subjectName}&academicLevel={academicLevel}&group={group}&paperType={paperType}") {
        fun createRoute(args: SelectedSubjectNavArgs): String {
            return args.createRoute("topic-analysis")
        }
    }
    data object AnswerBuilder : Screen("answer-builder/{subjectCode}?subjectName={subjectName}&academicLevel={academicLevel}&group={group}&paperType={paperType}") {
        fun createRoute(args: SelectedSubjectNavArgs): String {
            return args.createRoute("answer-builder")
        }
    }
    data object Suggestions : Screen("suggestions/{subjectCode}") {
        fun createRoute(subjectCode: String): String {
            return "suggestions/${Uri.encode(subjectCode)}"
        }
    }
    data object Analysis : Screen("analysis/{subjectCode}") {
        fun createRoute(subjectCode: String): String {
            return "analysis/${Uri.encode(subjectCode)}"
        }
    }
    data object Predictions : Screen("predictions/{subjectCode}?subjectName={subjectName}&academicLevel={academicLevel}&group={group}&paperType={paperType}") {
        fun createRoute(args: SelectedSubjectNavArgs): String {
            return args.createRoute("predictions")
        }
    }
    data object Questions : Screen("questions/{subjectCode}") {
        fun createRoute(subjectCode: String): String {
            return "questions/${Uri.encode(subjectCode)}"
        }
    }
    data object Answer : Screen("answer/{subjectCode}") {
        fun createRoute(subjectCode: String): String {
            return "answer/${Uri.encode(subjectCode)}"
        }
    }
}
