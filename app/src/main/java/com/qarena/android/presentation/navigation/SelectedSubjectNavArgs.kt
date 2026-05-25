package com.qarena.android.presentation.navigation

import android.net.Uri
import androidx.navigation.NavBackStackEntry

private const val ARG_SUBJECT_CODE = "subjectCode"
private const val ARG_SUBJECT_NAME = "subjectName"
private const val ARG_ACADEMIC_LEVEL = "academicLevel"
private const val ARG_GROUP = "group"
private const val ARG_PAPER_TYPE = "paperType"

data class SelectedSubjectNavArgs(
    val subjectCode: String,
    val subjectName: String? = null,
    val academicLevel: String? = null,
    val group: String? = null,
    val paperType: String? = null
) {
    fun createRoute(baseRoute: String): String {
        return buildString {
            append(baseRoute)
            append('/')
            append(Uri.encode(subjectCode))
            append("?subjectName=")
            append(Uri.encode(subjectName.orEmpty()))
            append("&academicLevel=")
            append(Uri.encode(academicLevel.orEmpty()))
            append("&group=")
            append(Uri.encode(group.orEmpty()))
            append("&paperType=")
            append(Uri.encode(paperType.orEmpty()))
        }
    }

    companion object {
        fun fromBackStackEntry(backStackEntry: NavBackStackEntry): SelectedSubjectNavArgs {
            return SelectedSubjectNavArgs(
                subjectCode = backStackEntry.arguments?.getString(ARG_SUBJECT_CODE).orEmpty(),
                subjectName = backStackEntry.arguments?.getString(ARG_SUBJECT_NAME),
                academicLevel = backStackEntry.arguments?.getString(ARG_ACADEMIC_LEVEL),
                group = backStackEntry.arguments?.getString(ARG_GROUP),
                paperType = backStackEntry.arguments?.getString(ARG_PAPER_TYPE)
            )
        }
    }
}
