package com.qarena.android.data.repository

import com.qarena.android.core.network.RetrofitClient
import com.qarena.android.core.session.SessionManager
import com.qarena.android.data.remote.ApiErrorParser
import com.qarena.android.data.remote.ApiException
import com.qarena.android.data.remote.api.SubjectApi
import com.qarena.android.data.remote.dto.QuestionListResponse
import com.qarena.android.data.remote.dto.QuestionResponse
import com.qarena.android.data.remote.dto.toQuestionResponse
import com.qarena.android.model.Subject
import com.qarena.android.util.PaperTypeLookups
import android.util.Log
import java.io.IOException
import retrofit2.HttpException

data class QuestionLoadResult(
    val questions: List<QuestionResponse>,
    val totalCount: Int
)

class QuestionRepository {

    private val subjectApi: SubjectApi = RetrofitClient.retrofit.create(SubjectApi::class.java)
    private val accessTokenProvider: () -> String? = { SessionManager.accessToken }

    suspend fun getQuestions(
        subjectCode: String,
        paperType: String? = null,
        debugScreenName: String? = null,
        subject: Subject? = null
    ): Result<QuestionLoadResult> {
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

            logQuestionRequest(
                screenName = debugScreenName ?: "Questions",
                subject = subject,
                subjectCode = trimmedSubjectCode,
                paperType = normalizedPaperType,
                limitApplied = false
            )

            Log.d(
                "SubjectQuestions",
                "subjectCode=$trimmedSubjectCode paperType=${normalizedPaperType ?: "null"}"
            )

            val response: QuestionListResponse = subjectApi.getQuestions(
                authorization = "Bearer $token",
                subjectCode = trimmedSubjectCode,
                paperType = normalizedPaperType ?: PaperTypeLookups.CQ
            )

            Log.d(
                "SubjectQuestions",
                "items=${response.items.size}, questions=${response.questions.size}, results=${response.results.size}, data=${response.data.size}"
            )

            val questions = response.questionList().map { it.toQuestionResponse() }

            logQuestionResponse(
                screenName = debugScreenName ?: "Questions",
                responseCount = questions.size,
                firstPaperType = questions.firstOrNull()?.paperType,
                totalPages = null,
                responsePath = "wrapper"
            )

            Log.d("SubjectQuestions", "total=${response.totalCount()} finalCount=${questions.size}")

            Result.success(
                QuestionLoadResult(
                    questions = questions,
                    totalCount = response.totalCount()
                )
            )
        } catch (exception: Exception) {
            logQuestionError(debugScreenName ?: "Questions", exception)
            Result.failure(Exception(mapThrowable(exception, normalizedPaperType != null)))
        }
    }

    private fun logQuestionRequest(
        screenName: String,
        subject: Subject?,
        subjectCode: String,
        paperType: String?,
        limitApplied: Boolean
    ) {
        Log.d(
            "SubjectRequest",
            buildString {
                append(screenName)
                append(" request_url=/subjects/")
                append(subjectCode)
                paperType?.let {
                    append("&paper_type=")
                    append(it)
                }
                append(" subject.id=")
                append(subject?.id ?: "null")
                append(" subject.subject_code=")
                append(subject?.subjectCode ?: subjectCode)
                append(" paper_type=")
                append(paperType ?: "null")
                append(" limit_applied=")
                append(limitApplied)
            }
        )
    }

    private fun logQuestionResponse(
        screenName: String,
        responseCount: Int,
        firstPaperType: String?,
        totalPages: Int?,
        responsePath: String
    ) {
        Log.d(
            "SubjectRequest",
            buildString {
                append(screenName)
                append(" response_count=")
                append(responseCount)
                append(" first_paper_type=")
                append(firstPaperType ?: "null")
                append(" response_path=")
                append(responsePath)
                append(" total_pages=")
                append(totalPages ?: "null")
            }
        )
    }

    private fun logQuestionError(screenName: String, exception: Exception) {
        val httpException = exception as? HttpException
        val errorBody = runCatching { httpException?.response()?.errorBody()?.string() }.getOrNull()
        Log.d(
            "SubjectRequest",
            buildString {
                append(screenName)
                append(" error code=")
                append(httpException?.code() ?: "null")
                append(" message=")
                append(exception.message ?: "null")
                if (!errorBody.isNullOrBlank()) {
                    append(" error_body=")
                    append(errorBody)
                }
            }
        )
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

        val parsedCode = ApiErrorParser.resolvedCode(parsed)
        if (!parsedCode.isNullOrBlank()) {
            val mapped = ApiErrorParser.messageForSubjectLookupCode(parsedCode, parsed.message)
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
            403 -> "You do not have access to this subject scope."
            404 -> "Questions not found for this subject."
            429 -> "Too many requests. Please try again later."
            in 500..599 -> "Server error. Please try again."
            else -> ApiErrorParser.messageForHttpStatus(code, "Failed to load questions")
        }
    }
}
