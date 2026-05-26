package com.qarena.android.data.repository

import android.util.Log
import com.qarena.android.core.network.RetrofitClient
import com.qarena.android.core.session.SessionManager
import com.qarena.android.data.remote.ApiErrorParser
import com.qarena.android.data.remote.ApiException
import com.qarena.android.data.remote.api.BoardPaperApi
import com.qarena.android.data.remote.dto.BoardPaperDetailResponse
import com.qarena.android.data.remote.dto.BoardPaperSummary
import java.io.IOException
import retrofit2.HttpException
import retrofit2.Response

class BoardPaperRepository(
    private val boardPaperApi: BoardPaperApi = RetrofitClient.retrofit.create(BoardPaperApi::class.java),
    private val accessTokenProvider: () -> String? = { SessionManager.accessToken }
) {

    suspend fun listAvailablePapers(academicLevel: String): Result<List<BoardPaperSummary>> {
        val token = accessTokenProvider()
        val trimmedAcademicLevel = academicLevel.trim()

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedAcademicLevel.isBlank()) {
            return Result.failure(Exception("Academic level is required"))
        }

        return try {
            val response = boardPaperApi.listBoardPapers(
                authorization = "Bearer $token",
                academicLevel = trimmedAcademicLevel
            )

            Result.success(response.papers)
        } catch (exception: Exception) {
            Log.d(
                "BoardPaperRepository",
                "listAvailablePapers error=${exception.message ?: exception::class.java.simpleName}"
            )
            Result.failure(Exception(mapThrowable(exception, fallbackMessage = "Failed to load board papers.", isSelectedPaperRequest = false)))
        }
    }

    suspend fun getPaperById(examId: Int): Result<BoardPaperDetailResponse> {
        val token = accessTokenProvider()

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (examId <= 0) {
            return Result.failure(Exception("Exam id is required"))
        }

        return try {
            val response = boardPaperApi.getBoardPaperById(
                authorization = "Bearer $token",
                examId = examId
            )

            Result.success(response)
        } catch (exception: Exception) {
            Log.d(
                "BoardPaperRepository",
                "getPaperById error examId=$examId message=${exception.message ?: exception::class.java.simpleName}"
            )
            Result.failure(Exception(mapThrowable(exception, fallbackMessage = "Failed to load selected paper.", isSelectedPaperRequest = true)))
        }
    }

    private fun mapThrowable(
        exception: Exception,
        fallbackMessage: String,
        isSelectedPaperRequest: Boolean
    ): String {
        return when (exception) {
            is ApiException -> exception.message ?: fallbackMessage
            is HttpException -> parseBoardPaperApiError(exception.response(), fallbackMessage, isSelectedPaperRequest)
            is IOException -> ApiErrorParser.messageForThrowable(exception)
            else -> exception.message ?: fallbackMessage
        }
    }

    private fun parseBoardPaperApiError(
        response: Response<*>?,
        fallbackMessage: String,
        isSelectedPaperRequest: Boolean
    ): String {
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
            400 -> fallbackMessage
            401 -> "Session expired. Please log in again."
            403 -> "You do not have access to board papers."
            404 -> if (isSelectedPaperRequest) {
                "No paper found for this board, year, and subject."
            } else {
                fallbackMessage
            }
            429 -> "Too many requests. Please try again later."
            in 500..599 -> "Server error. Please try again."
            else -> ApiErrorParser.messageForHttpStatus(code, fallbackMessage)
        }
    }
}