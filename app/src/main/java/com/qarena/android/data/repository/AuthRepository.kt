package com.qarena.android.data.repository

import com.qarena.android.core.network.RetrofitClient
import com.qarena.android.data.remote.api.AuthApi
import com.qarena.android.data.remote.dto.LoginRequest
import com.qarena.android.data.remote.dto.LoginResponse
import com.qarena.android.data.remote.dto.UserResponse

class AuthRepository {

    private val authApi: AuthApi = RetrofitClient.retrofit.create(AuthApi::class.java)

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(
                email = email,
                password = password
            )
            val response = authApi.login(request)
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getCurrentUser(token: String): Result<UserResponse> {
        return try {
            val response = authApi.getCurrentUser(
                authorization = "Bearer $token"
            )
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
