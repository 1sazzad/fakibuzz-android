package com.qarena.android.data.repository

import com.qarena.android.core.network.RetrofitClient
import com.qarena.android.core.session.SessionManager
import com.qarena.android.data.remote.ApiErrorParser
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
            val enrichedRequest = request.copy(
                academicLevel = request.academicLevel ?: backendAcademicLevel(SessionManager.userAcademicLevel),
                universityId = request.universityId ?: SessionManager.userUniversityId,
                departmentId = request.departmentId ?: SessionManager.userDepartmentId,
                subjectCode = request.subjectCode?.trim()?.takeIf { it.isNotBlank() },
                questionText = request.questionText?.trim()?.takeIf { it.isNotBlank() },
                paperType = request.paperType?.trim()?.takeIf { it.isNotBlank() },
                topic = request.topic?.trim()?.takeIf { it.isNotBlank() },
                answerType = request.answerType?.trim()?.takeIf { it.isNotBlank() },
                formulaLatex = request.formulaLatex?.trim()?.takeIf { it.isNotBlank() },
                formulaDisplay = request.formulaDisplay?.trim()?.takeIf { it.isNotBlank() },
                diagramType = request.diagramType?.trim()?.takeIf { it.isNotBlank() },
                diagramSvg = request.diagramSvg?.trim()?.takeIf { it.isNotBlank() },
                diagramUrl = request.diagramUrl?.trim()?.takeIf { it.isNotBlank() },
                diagramReference = request.diagramReference?.trim()?.takeIf { it.isNotBlank() },
                diagramDescription = request.diagramDescription?.trim()?.takeIf { it.isNotBlank() },
                prompt = request.prompt?.trim()?.takeIf { it.isNotBlank() }
            )
            val response = answerApi.generateAnswer(
                authorization = "Bearer $token",
                generateAnswerRequest = enrichedRequest
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
            val statusMessage = ApiErrorParser.messageForHttpStatus(code(), "")
            if (statusMessage.isNotBlank()) {
                return statusMessage
            }

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

    private fun backendAcademicLevel(value: String?): String? {
        return when (value?.trim()?.lowercase()) {
            "ssc" -> "SSC"
            "hsc" -> "HSC"
            "university" -> "UNIVERSITY"
            else -> value?.trim()?.takeIf { it.isNotBlank() }
        }
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
