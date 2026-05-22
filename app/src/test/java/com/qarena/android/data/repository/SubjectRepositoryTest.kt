package com.qarena.android.data.repository

import com.qarena.android.data.remote.api.SubjectApi
import com.qarena.android.data.remote.dto.PredictionDto
import com.qarena.android.data.remote.dto.PredictionsResponse
import com.qarena.android.data.remote.dto.QuestionListResponse
import com.qarena.android.data.remote.dto.SubjectAnalysisResponse
import com.qarena.android.data.remote.dto.SubjectListResponse
import com.qarena.android.data.remote.dto.SubjectOverviewResponse
import com.qarena.android.data.remote.dto.SuggestionDto
import com.qarena.android.data.remote.dto.SuggestionsResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class SubjectRepositoryTest {

    @Test
    fun blankQueryIsBlockedBeforeApiCall() = runBlocking {
        val fakeApi = FakeSubjectApi()
        val repository = SubjectRepository(fakeApi) { "token" }

        val result = repository.getSubjectSuggestions(
            subjectCode = "CSE101",
            query = "   ",
            topK = 10
        )

        assertTrue(result.isFailure)
        assertEquals("Search query is required", result.exceptionOrNull()?.message)
        assertEquals(0, fakeApi.suggestionsCallCount)
    }

    @Test
    fun topKIsClampedBeforeSuggestionsApiCall() = runBlocking {
        val fakeApi = FakeSubjectApi()
        val repository = SubjectRepository(fakeApi) { "token" }

        val result = repository.getSubjectSuggestions(
            subjectCode = " CSE101 ",
            query = "important questions",
            topK = 999
        )

        assertTrue(result.isSuccess)
        assertEquals(50, fakeApi.lastSuggestionsTopK)
    }

    private class FakeSubjectApi : SubjectApi {
        var suggestionsCallCount: Int = 0
            private set
        var lastSuggestionsTopK: Int? = null
            private set

        override suspend fun getSubjects(authorization: String): SubjectListResponse {
            error("Not used")
        }

        override suspend fun searchSubjects(
            authorization: String,
            query: String
        ): SubjectListResponse {
            error("Not used")
        }

        override suspend fun getSubjectOverview(
            authorization: String,
            subjectCode: String
        ): SubjectOverviewResponse {
            error("Not used")
        }

        override suspend fun getSubjectAnalysis(
            authorization: String,
            subjectCode: String
        ): SubjectAnalysisResponse {
            error("Not used")
        }

        override suspend fun getPredictions(
            authorization: String,
            subjectCode: String
        ): Response<PredictionsResponse> {
            return Response.success(
                PredictionsResponse(
                    success = true,
                    items = listOf(
                        PredictionDto(
                            question_id = 1,
                            question_text = "Likely question",
                            topic = "Topic",
                            marks = 10,
                            year = 2024,
                            prediction_score = 0.81
                        )
                    ),
                    predictions = null
                )
            )
        }

        override suspend fun getQuestions(
            authorization: String,
            subjectCode: String,
            page: Int,
            limit: Int,
            paperType: String?
        ): QuestionListResponse {
            error("Not used")
        }

        override suspend fun getSuggestions(
            authorization: String,
            subjectCode: String,
            query: String,
            topK: Int,
            paperType: String?
        ): Response<SuggestionsResponse> {
            suggestionsCallCount += 1
            lastSuggestionsTopK = topK

            return Response.success(
                SuggestionsResponse(
                    success = true,
                    items = listOf(
                        SuggestionDto(
                            question_id = 1,
                            question_text = "Important question",
                            topic = "Core topic",
                            marks = 10,
                            year = 2024,
                            prediction_score = 0.91
                        )
                    ),
                    suggestions = null
                )
            )
        }
    }
}
