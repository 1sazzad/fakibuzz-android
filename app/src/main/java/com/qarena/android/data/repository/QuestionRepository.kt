package com.qarena.android.data.repository

import com.qarena.android.core.network.RetrofitClient
import com.qarena.android.core.session.SessionManager
import com.qarena.android.data.remote.api.QuestionApi
import com.qarena.android.data.remote.dto.QuestionResponse

class QuestionRepository {

    private val questionApi: QuestionApi = RetrofitClient.retrofit.create(QuestionApi::class.java)

    suspend fun getQuestions(
        subjectCode: String,
        page: Int = 1,
        limit: Int = 20
    ): Result<List<QuestionResponse>> {
        val token = SessionManager.accessToken
        val trimmedSubjectCode = subjectCode.trim()

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedSubjectCode.isBlank()) {
            return Result.failure(Exception("Subject code is required"))
        }

        return try {
            val response = questionApi.getQuestions(
                authorization = "Bearer $token",
                subjectCode = trimmedSubjectCode,
                page = page,
                limit = limit
            )
            Result.success(response.questions ?: emptyList())
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
