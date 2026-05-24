package com.qarena.android.data.repository

import com.qarena.android.core.network.RetrofitClient
import com.qarena.android.core.session.SessionManager
import com.qarena.android.data.remote.ApiErrorParser
import com.qarena.android.data.remote.api.SearchApi
import com.qarena.android.data.remote.dto.SearchRequest
import com.qarena.android.data.remote.dto.SearchResponse
import retrofit2.HttpException

class SearchRepository {

    private val searchApi: SearchApi = RetrofitClient.retrofit.create(SearchApi::class.java)

    suspend fun searchQuestions(request: SearchRequest): Result<SearchResponse> {
        val token = SessionManager.accessToken
        val trimmedQuery = request.query.trim()

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedQuery.isBlank()) {
            return Result.failure(Exception("Search query is required"))
        }

        return try {
            val academicLevel = backendAcademicLevel(SessionManager.userAcademicLevel)
            val response = searchApi.search(
                authorization = "Bearer $token",
                searchRequest = request.copy(
                    query = trimmedQuery,
                    subjectCode = request.subjectCode?.trim()?.takeIf { it.isNotBlank() },
                    academicLevel = request.academicLevel ?: academicLevel,
                    paperType = request.paperType?.trim()?.takeIf { it.isNotBlank() },
                    universityId = request.universityId ?: SessionManager.userUniversityId,
                    departmentId = request.departmentId ?: SessionManager.userDepartmentId
                )
            )
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(Exception(exception.toReadableMessage()))
        }
    }

    private fun Exception.toReadableMessage(): String {
        if (this is HttpException) {
            return ApiErrorParser.messageForHttpStatus(
                statusCode = code(),
                fallback = message()
                    ?.takeIf { it.isNotBlank() }
                    ?: "Search failed"
            )
        }

        return ApiErrorParser.messageForThrowable(this)
    }

    private fun backendAcademicLevel(value: String?): String? {
        return when (value?.trim()?.lowercase()) {
            "ssc" -> "SSC"
            "hsc" -> "HSC"
            "university" -> "UNIVERSITY"
            else -> value?.trim()?.takeIf { it.isNotBlank() }
        }
    }
}
