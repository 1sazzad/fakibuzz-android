package com.qarena.android.data.remote.api

import com.qarena.android.data.remote.dto.SubjectListResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface SubjectApi {

    @GET("subjects")
    suspend fun getSubjects(
        @Header("Authorization") authorization: String
    ): SubjectListResponse
}
