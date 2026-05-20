package com.qarena.android.data.remote.api

import com.qarena.android.data.remote.dto.LoginRequest
import com.qarena.android.data.remote.dto.LoginResponse
import com.qarena.android.data.remote.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): LoginResponse

    @GET("auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): UserResponse
}
