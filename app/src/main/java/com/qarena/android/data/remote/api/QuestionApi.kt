package com.qarena.android.data.remote.api

import com.qarena.android.data.remote.dto.QuestionListResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface QuestionApi {

    @GET("subjects/{subjectCode}/questions")
    suspend fun getQuestions(
        @Header("Authorization") authorization: String,
        @Path("subjectCode") subjectCode: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): QuestionListResponse
}
