package com.qarena.android.data.remote.api

import com.qarena.android.data.remote.dto.LoginRequest
import com.qarena.android.data.remote.dto.ForgotPasswordRequest
import com.qarena.android.data.remote.dto.ForgotPasswordResponse
import com.qarena.android.data.remote.dto.MessageResponse
import com.qarena.android.data.remote.dto.ProfileUpdateRequest
import com.qarena.android.data.remote.dto.RegisterRequest
import com.qarena.android.data.remote.dto.RegisterResponse
import com.qarena.android.data.remote.dto.ResendVerificationEmailRequest
import com.qarena.android.data.remote.dto.ResetPasswordRequest
import com.qarena.android.data.remote.dto.ResetPasswordResponse
import com.qarena.android.data.remote.dto.TokenResponse
import com.qarena.android.data.remote.dto.UserResponse
import com.qarena.android.data.remote.dto.VerifyEmailRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.Response

interface AuthApi {

    @POST("auth/register")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): Response<RegisterResponse>

    @POST("auth/verify-email")
    suspend fun verifyEmail(
        @Body verifyEmailRequest: VerifyEmailRequest
    ): MessageResponse

    @POST("auth/resend-verification-email")
    suspend fun resendVerificationEmail(
        @Body resendVerificationEmailRequest: ResendVerificationEmailRequest
    ): MessageResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body forgotPasswordRequest: ForgotPasswordRequest
    ): ForgotPasswordResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body resetPasswordRequest: ResetPasswordRequest
    ): ResetPasswordResponse

    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<TokenResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): UserResponse

    @PATCH("auth/me/profile")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body profileUpdateRequest: ProfileUpdateRequest
    ): UserResponse
}
