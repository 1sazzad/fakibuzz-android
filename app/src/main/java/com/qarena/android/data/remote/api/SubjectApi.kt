package com.qarena.android.data.remote.api

import com.qarena.android.data.remote.dto.QuestionListResponse
import com.qarena.android.data.remote.dto.PredictionsResponse
import com.qarena.android.data.remote.dto.SuggestionsResponse
import com.qarena.android.data.remote.dto.SubjectAnalysisResponse
import com.qarena.android.data.remote.dto.SubjectOverviewResponse
import com.qarena.android.data.remote.dto.SubjectListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface SubjectApi {

    // TODO: Add suggestions export PDF endpoints when Android adds a dedicated export/download flow.

    @GET("subjects")
    suspend fun getSubjects(
        @Header("Authorization") authorization: String
    ): SubjectListResponse

    @GET("subjects/search")
    suspend fun searchSubjects(
        @Header("Authorization") authorization: String,
        @Query("query") query: String
    ): SubjectListResponse

    @GET("subjects/{subjectCode}/overview")
    suspend fun getSubjectOverview(
        @Header("Authorization") authorization: String,
        @Path("subjectCode") subjectCode: String
    ): SubjectOverviewResponse

    @GET("subjects/{subjectCode}/analysis")
    suspend fun getSubjectAnalysis(
        @Header("Authorization") authorization: String,
        @Path("subjectCode") subjectCode: String
    ): SubjectAnalysisResponse

    @GET("subjects/{subjectCode}/predictions")
    suspend fun getPredictions(
        @Header("Authorization") authorization: String,
        @Path("subjectCode") subjectCode: String
    ): Response<PredictionsResponse>

    @GET("subjects/{subjectCode}/questions")
    suspend fun getQuestions(
        @Header("Authorization") authorization: String,
        @Path("subjectCode") subjectCode: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("paper_type") paperType: String? = null
    ): QuestionListResponse

    @GET("subjects/{subjectCode}/suggestions")
    suspend fun getSuggestions(
        @Header("Authorization") authorization: String,
        @Path("subjectCode") subjectCode: String,
        @Query("query") query: String,
        @Query("top_k") topK: Int,
        @Query("paper_type") paperType: String? = null
    ): Response<SuggestionsResponse>
}
