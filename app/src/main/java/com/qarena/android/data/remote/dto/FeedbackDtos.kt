package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FeedbackRequest(
    val name: String? = null,
    val email: String? = null,
    val rating: Int? = null,
    val message: String? = null,

    @SerializedName("page_url")
    val pageUrl: String? = null,

    @SerializedName("feedback_type")
    val feedbackType: String? = null,

    @SerializedName("question_id")
    val questionId: Int? = null,

    @SerializedName("subject_code")
    val subjectCode: String? = null
)

data class FeedbackResponse(
    val id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val message: String? = null,
    val rating: Int? = null,
    val status: String? = null,

    @SerializedName("page_url")
    val pageUrl: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null
)

data class FeedbackListResponse(
    val feedback: List<FeedbackResponse> = emptyList(),
    val total: Int? = null,
    val limit: Int? = null
)

data class AnalyticsVisitRequest(
    @SerializedName("session_id")
    val sessionId: String? = null,

    val screen: String? = null,

    @SerializedName("screen_name")
    val screenName: String? = null,

    val path: String? = null,
    val platform: String? = null,

    @SerializedName("subject_code")
    val subjectCode: String? = null,

    @SerializedName("question_id")
    val questionId: Int? = null,

    val metadata: Map<String, String>? = null
)

data class AnalyticsVisitResponse(
    val message: String? = null,
    val success: Boolean? = null,

    @SerializedName("visit_id")
    val visitId: String? = null
)
