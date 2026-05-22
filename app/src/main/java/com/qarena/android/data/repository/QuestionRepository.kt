package com.qarena.android.data.repository

import com.qarena.android.core.network.RetrofitClient
import com.qarena.android.core.session.SessionManager
import com.qarena.android.data.remote.ApiErrorParser
import com.qarena.android.data.remote.ApiException
import com.qarena.android.data.remote.api.SubjectApi
import com.qarena.android.data.remote.dto.QuestionResponse
import com.qarena.android.util.PaperTypeLookups
import java.io.IOException
import retrofit2.HttpException

class QuestionRepository {

    private val subjectApi: SubjectApi = RetrofitClient.retrofit.create(SubjectApi::class.java)

    private val accessTokenProvider: () -> String? = { SessionManager.accessToken }

    suspend fun getQuestions(
        subjectCode: String,
        page: Int = 1,
        limit: Int = 20,
        paperType: String? = null
    ): Result<List<QuestionResponse>> {
        val token = accessTokenProvider()
        val trimmedSubjectCode = subjectCode.trim()
        val normalizedPaperType = PaperTypeLookups.normalizePaperType(paperType)

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedSubjectCode.isBlank()) {
            return Result.failure(Exception("Subject code is required"))
        }

        if (paperType != null && normalizedPaperType == null) {
            return Result.failure(Exception("Invalid paper type. Please choose CQ, MCQ, or WRITTEN."))
        }

        return try {
            val response = subjectApi.getQuestions(
                authorization = "Bearer $token",
                subjectCode = trimmedSubjectCode,
                page = page,
                limit = limit,
                paperType = normalizedPaperType
            )
            Result.success(response.questions ?: emptyList())
        } catch (exception: Exception) {
            Result.failure(Exception(mapThrowable(exception, normalizedPaperType != null)))
        }
    }

    private fun mapThrowable(exception: Exception, hasPaperType: Boolean): String {
        return when (exception) {
            is ApiException -> exception.message ?: "Failed to load questions"
            is HttpException -> parseQuestionApiError(exception.response(), hasPaperType)
            is IOException -> ApiErrorParser.messageForThrowable(exception)
            else -> exception.message ?: "Failed to load questions"
        }
    }

    private fun parseQuestionApiError(response: retrofit2.Response<*>?, hasPaperType: Boolean): String {
        val code = response?.code() ?: 0
        val errorBody = runCatching { response?.errorBody()?.string() }.getOrNull()
        val parsed = ApiErrorParser.parseErrorBody(errorBody)

        if (!parsed.code.isNullOrBlank()) {
            val mapped = ApiErrorParser.messageForSubjectLookupCode(parsed.code, parsed.message)
            if (!mapped.isNullOrBlank()) {
                return mapped
            }
        }

        return when (code) {
            400 -> if (hasPaperType) {
                "Invalid paper type. Please choose CQ, MCQ, or WRITTEN."
            } else {
                "Please provide a valid subject and try again."
            }

            401 -> "Session expired. Please log in again."
            404 -> "Questions not found for this subject."
            429 -> "Too many requests. Please try again later."
            in 500..599 -> "Server error. Please try again."
            else -> "Failed to load questions"
        }
    }
}
