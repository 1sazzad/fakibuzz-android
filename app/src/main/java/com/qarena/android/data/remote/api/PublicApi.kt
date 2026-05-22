package com.qarena.android.data.remote.api

import com.google.gson.JsonElement
import com.qarena.android.data.remote.dto.HealthResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface PublicApi {

    @GET("universities")
    suspend fun getUniversities(): JsonElement

    @GET("universities/{universityId}/departments")
    suspend fun getDepartments(
        @Path("universityId") universityId: Int
    ): JsonElement

    @GET("health")
    suspend fun getHealth(): HealthResponse
}
