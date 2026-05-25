package com.qarena.android.data.remote.api

import com.qarena.android.data.remote.dto.PredictionListResponse
import com.qarena.android.data.remote.dto.QuestionListResponse
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

    @GET("subjects?status=active")
    suspend fun getSubjects(
        @Header("Authorization") authorization: String,
        @Query("academic_level") academicLevel: String? = null,
        @Query("university_id") universityId: Int? = null,
        @Query("department_id") departmentId: Int? = null,
        @Query("curriculum") curriculum: String? = null,
        @Query("stream_group") streamGroup: String? = null
    ): SubjectListResponse

    @GET("subjects/search?status=active")
    suspend fun searchSubjects(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("academic_level") academicLevel: String? = null,
        @Query("university_id") universityId: Int? = null,
        @Query("department_id") departmentId: Int? = null,
        @Query("curriculum") curriculum: String? = null,
        @Query("stream_group") streamGroup: String? = null
    ): SubjectListResponse

    @GET("subjects/{subjectCode}/overview")
    suspend fun getSubjectOverview(
        @Header("Authorization") authorization: String,
        @Path("subjectCode") subjectCode: String,
        @Query("academic_level") academicLevel: String? = null,
        @Query("paper_type") paperType: String? = null,
        @Query("curriculum") curriculum: String? = null,
        @Query("stream_group") streamGroup: String? = null
    ): SubjectOverviewResponse

    @GET("subjects/{subjectCode}/analysis")
    suspend fun getSubjectAnalysis(
        @Header("Authorization") authorization: String,
        @Path("subjectCode") subjectCode: String,
        @Query("academic_level") academicLevel: String? = null,
        @Query("paper_type") paperType: String? = null,
        @Query("curriculum") curriculum: String? = null,
        @Query("stream_group") streamGroup: String? = null
    ): SubjectAnalysisResponse

    @GET("subjects/{subjectCode}/predictions")
    suspend fun getPredictions(
        @Header("Authorization") authorization: String,
        @Path("subjectCode") subjectCode: String,
        @Query("academic_level") academicLevel: String? = null,
        @Query("paper_type") paperType: String? = null,
        @Query("university_id") universityId: Int? = null,
        @Query("department_id") departmentId: Int? = null,
        @Query("curriculum") curriculum: String? = null,
        @Query("stream_group") streamGroup: String? = null
    ): Response<PredictionListResponse>

    @GET("subjects/{subjectCode}/questions")
    suspend fun getQuestions(
        @Header("Authorization") authorization: String,
        @Path("subjectCode") subjectCode: String,
        @Query("paper_type") paperType: String
    ): QuestionListResponse

    @GET("subjects/{subject_code}/suggestions")
    suspend fun getSuggestions(
        @Header("Authorization") authorization: String,
        @Path("subject_code") subjectCode: String,
        @Query("query") query: String,
        @Query("top_k") topK: Int,
        @Query("paper_type") paperType: String? = null,
        @Query("academic_level") academicLevel: String? = null,
        @Query("university_id") universityId: Int? = null,
        @Query("department_id") departmentId: Int? = null,
        @Query("curriculum") curriculum: String? = null,
        @Query("stream_group") streamGroup: String? = null
    ): Response<SuggestionsResponse>
}
