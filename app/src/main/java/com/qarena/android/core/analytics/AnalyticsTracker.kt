package com.qarena.android.core.analytics

import com.qarena.android.data.remote.dto.AnalyticsVisitRequest
import com.qarena.android.data.repository.FeedbackRepository
import java.util.UUID

object AnalyticsTracker {

    private val sessionId = UUID.randomUUID().toString()
    private val feedbackRepository = FeedbackRepository()

    suspend fun trackScreen(
        screenName: String,
        path: String,
        subjectCode: String? = null,
        questionId: Int? = null
    ) {
        runCatching {
            feedbackRepository.trackVisit(
                AnalyticsVisitRequest(
                    sessionId = sessionId,
                    screen = screenName,
                    screenName = screenName,
                    path = path,
                    platform = PLATFORM,
                    subjectCode = subjectCode?.takeIf { it.isNotBlank() },
                    questionId = questionId,
                    metadata = mapOf("platform" to PLATFORM)
                )
            )
        }
    }

    private const val PLATFORM = "android"
}
