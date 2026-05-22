package com.qarena.android.data.repository

import com.qarena.android.core.network.RetrofitClient
import com.qarena.android.core.session.SessionManager
import com.qarena.android.data.remote.api.FeedbackApi
import com.qarena.android.data.remote.dto.AnalyticsVisitRequest
import com.qarena.android.data.remote.dto.AnalyticsVisitResponse
import com.qarena.android.data.remote.dto.FeedbackListResponse
import com.qarena.android.data.remote.dto.FeedbackRequest
import com.qarena.android.data.remote.dto.FeedbackResponse

class FeedbackRepository {

    private val feedbackApi: FeedbackApi = RetrofitClient.retrofit.create(FeedbackApi::class.java)

    suspend fun submitFeedback(request: FeedbackRequest): Result<FeedbackResponse> {
        val token = SessionManager.accessToken

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        return try {
            val response = feedbackApi.submitFeedback(
                authorization = "Bearer $token",
                feedbackRequest = request
            )
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getPublicFeedback(limit: Int = 10): Result<FeedbackListResponse> {
        if (limit <= 0) {
            return Result.failure(Exception("Limit must be greater than zero"))
        }

        return try {
            val response = feedbackApi.getPublicFeedback(limit)
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun trackVisit(request: AnalyticsVisitRequest): Result<AnalyticsVisitResponse> {
        return try {
            val response = feedbackApi.trackVisit(request)
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
