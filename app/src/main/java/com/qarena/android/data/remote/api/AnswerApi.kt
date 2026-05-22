package com.qarena.android.data.remote.api

import com.qarena.android.data.remote.dto.GenerateAnswerRequest
import com.qarena.android.data.remote.dto.GenerateAnswerResponse
import com.qarena.android.data.remote.dto.JobResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AnswerApi {

    @POST("generate-answer")
    suspend fun generateAnswer(
        @Header("Authorization") authorization: String,
        @Body generateAnswerRequest: GenerateAnswerRequest
    ): GenerateAnswerResponse

    @GET("jobs/{jobId}")
    suspend fun getJob(
        @Header("Authorization") authorization: String,
        @Path("jobId") jobId: String
    ): JobResponse
}
