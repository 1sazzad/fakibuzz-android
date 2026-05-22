package com.qarena.android.data.repository

import com.qarena.android.core.network.RetrofitClient
import com.qarena.android.core.session.SessionManager
import com.qarena.android.data.remote.api.AnswerApi
import com.qarena.android.data.remote.dto.GenerateAnswerRequest
import com.qarena.android.data.remote.dto.GenerateAnswerResponse
import com.qarena.android.data.remote.dto.JobResponse
import org.json.JSONObject
import retrofit2.HttpException

class AnswerRepository {

    private val answerApi: AnswerApi = RetrofitClient.retrofit.create(AnswerApi::class.java)

    suspend fun generateAnswer(request: GenerateAnswerRequest): Result<GenerateAnswerResponse> {
        val token = SessionManager.accessToken

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        return try {
            val response = answerApi.generateAnswer(
                authorization = "Bearer $token",
                generateAnswerRequest = request
            )
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(Exception(exception.readableMessage("Failed to generate answer")))
        }
    }

    suspend fun getJobStatus(jobId: String): Result<JobResponse> {
        val token = SessionManager.accessToken
        val trimmedJobId = jobId.trim()

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedJobId.isBlank()) {
            return Result.failure(Exception("Job id is required"))
        }

        return try {
            val response = answerApi.getJob(
                authorization = "Bearer $token",
                jobId = trimmedJobId
            )
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(Exception(exception.readableMessage("Failed to load job status")))
        }
    }

    private fun Exception.readableMessage(fallback: String): String {
        if (this is HttpException) {
            val errorBody = response()?.errorBody()?.string()
            val backendMessage = errorBody?.extractBackendMessage()

            if (!backendMessage.isNullOrBlank()) {
                return backendMessage
            }

            return message().takeIf { it.isNotBlank() }
                ?: "Request failed with status code ${code()}"
        }

        return message ?: fallback
    }

    private fun String.extractBackendMessage(): String? {
        return runCatching {
            val json = JSONObject(this)
            json.optString("message")
                .ifBlank { json.optString("error") }
                .ifBlank { json.optString("detail") }
                .takeIf { it.isNotBlank() }
        }.getOrNull() ?: takeIf { it.isNotBlank() }
    }
}
