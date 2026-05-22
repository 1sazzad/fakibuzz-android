package com.qarena.android.data.remote.api

import com.qarena.android.data.remote.dto.AnalyticsVisitRequest
import com.qarena.android.data.remote.dto.AnalyticsVisitResponse
import com.qarena.android.data.remote.dto.FeedbackListResponse
import com.qarena.android.data.remote.dto.FeedbackRequest
import com.qarena.android.data.remote.dto.FeedbackResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface FeedbackApi {

    @POST("feedback")
    suspend fun submitFeedback(
        @Header("Authorization") authorization: String,
        @Body feedbackRequest: FeedbackRequest
    ): FeedbackResponse

    @GET("feedback/public")
    suspend fun getPublicFeedback(
        @Query("limit") limit: Int
    ): FeedbackListResponse

    @POST("analytics/visit")
    suspend fun trackVisit(
        @Body analyticsVisitRequest: AnalyticsVisitRequest
    ): AnalyticsVisitResponse
}
