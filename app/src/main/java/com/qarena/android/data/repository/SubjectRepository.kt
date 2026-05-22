package com.qarena.android.data.repository

import com.qarena.android.core.network.RetrofitClient
import com.qarena.android.core.session.SessionManager
import com.qarena.android.data.remote.ApiErrorParser
import com.qarena.android.data.remote.ApiException
import com.qarena.android.data.remote.api.SubjectApi
import com.qarena.android.data.remote.dto.SubjectAnalysisResponse
import com.qarena.android.data.remote.dto.SubjectOverviewResponse
import com.qarena.android.data.remote.dto.SubjectListResponse
import com.qarena.android.model.Subject
import com.qarena.android.model.Suggestion
import com.qarena.android.util.PaperTypeLookups
import com.qarena.android.util.SubjectLookups
import com.qarena.android.util.SuggestionLookups
import java.io.IOException
import retrofit2.HttpException
import retrofit2.Response

class SubjectRepository(
    private val subjectApi: SubjectApi = RetrofitClient.retrofit.create(SubjectApi::class.java),
    private val accessTokenProvider: () -> String? = { SessionManager.accessToken }
) {

    suspend fun getSubjects(): Result<List<Subject>> {
        val token = accessTokenProvider()

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        return try {
            val response = subjectApi.getSubjects(
                authorization = "Bearer $token"
            )
            val subjects = SubjectLookups.normalizeSubjects(response)
            Result.success(subjects)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun searchSubjects(query: String): Result<List<Subject>> {
        val token = accessTokenProvider()
        val trimmedQuery = query.trim()

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedQuery.isBlank()) {
            return Result.failure(Exception("Search query is required"))
        }

        return try {
            val response = subjectApi.searchSubjects(
                authorization = "Bearer $token",
                query = trimmedQuery
            )
            val subjects = SubjectLookups.normalizeSubjects(response)
            Result.success(subjects)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getSubjectOverview(subjectCode: String): Result<SubjectOverviewResponse> {
        val token = accessTokenProvider()
        val trimmedSubjectCode = subjectCode.trim()

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedSubjectCode.isBlank()) {
            return Result.failure(Exception("Subject code is required"))
        }

        return try {
            val response = subjectApi.getSubjectOverview(
                authorization = "Bearer $token",
                subjectCode = trimmedSubjectCode
            )
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getSubjectAnalysis(subjectCode: String): Result<SubjectAnalysisResponse> {
        val token = accessTokenProvider()
        val trimmedSubjectCode = subjectCode.trim()

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedSubjectCode.isBlank()) {
            return Result.failure(Exception("Subject code is required"))
        }

        return try {
            val response = subjectApi.getSubjectAnalysis(
                authorization = "Bearer $token",
                subjectCode = trimmedSubjectCode
            )
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getSubjectPredictions(subjectCode: String): Result<List<Suggestion>> {
        val token = accessTokenProvider()
        val trimmedSubjectCode = subjectCode.trim()

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedSubjectCode.isBlank()) {
            return Result.failure(Exception("Subject code is required"))
        }

        return try {
            val response = subjectApi.getPredictions(
                authorization = "Bearer $token",
                subjectCode = trimmedSubjectCode
            )
            if (!response.isSuccessful) {
                return Result.failure(Exception(parseSubjectApiError(response, "Failed to load subject predictions")))
            }

            val normalized = SuggestionLookups.normalizePredictions(response.body())
            Result.success(normalized)
        } catch (exception: Exception) {
            Result.failure(Exception(mapThrowable(exception, "Failed to load subject predictions")))
        }
    }

    suspend fun getSubjectSuggestions(
        subjectCode: String,
        query: String,
        topK: Int = 5,
        paperType: String? = null
    ): Result<List<Suggestion>> {
        val token = accessTokenProvider()
        val trimmedSubjectCode = subjectCode.trim()
        val normalizedQuery = SuggestionLookups.normalizeQuery(query)
        val clampedTopK = SuggestionLookups.clampTopK(topK)
        val normalizedPaperType = PaperTypeLookups.normalizePaperType(paperType)

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedSubjectCode.isBlank()) {
            return Result.failure(Exception("Subject code is required"))
        }

        if (normalizedQuery.isBlank()) {
            return Result.failure(Exception("Search query is required"))
        }

        if (paperType != null && normalizedPaperType == null) {
            return Result.failure(Exception("Invalid paper type. Please choose CQ, MCQ, or WRITTEN."))
        }

        return try {
            val response = subjectApi.getSuggestions(
                authorization = "Bearer $token",
                subjectCode = trimmedSubjectCode,
                query = normalizedQuery,
                topK = clampedTopK,
                paperType = normalizedPaperType
            )
            if (!response.isSuccessful) {
                return Result.failure(Exception(parseSubjectApiError(response, "Failed to load subject suggestions")))
            }

            val normalized = SuggestionLookups.normalizeSuggestions(response.body())
            Result.success(normalized)
        } catch (exception: Exception) {
            Result.failure(Exception(mapThrowable(exception, "Failed to load subject suggestions")))
        }
    }

    private fun parseSubjectApiError(response: Response<*>?, fallback: String): String {
        val code = response?.code() ?: 0
        val errorBody = runCatching { response?.errorBody()?.string() }.getOrNull()
        val parsed = ApiErrorParser.parseErrorBody(errorBody)

        val messageFromCode = ApiErrorParser.messageForSubjectLookupCode(
            code = parsed.code,
            fallback = parsed.message
        )

        if (!messageFromCode.isNullOrBlank()) {
            return messageFromCode
        }

        return when (code) {
            400 -> "Invalid paper type. Please choose CQ, MCQ, or WRITTEN."
            401 -> "Session expired. Please log in again."
            404 -> "Subject not found or inactive."
            429 -> "Too many requests. Please try again later."
            in 500..599 -> "Server error. Please try again."
            else -> fallback
        }
    }

    private fun mapThrowable(exception: Exception, fallback: String): String {
        return when (exception) {
            is ApiException -> exception.message ?: fallback
            is HttpException -> parseSubjectApiError(exception.response(), fallback)
            is IOException -> ApiErrorParser.messageForThrowable(exception)
            else -> exception.message ?: fallback
        }
    }
}
