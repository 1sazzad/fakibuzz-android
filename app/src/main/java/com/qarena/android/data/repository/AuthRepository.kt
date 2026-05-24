package com.qarena.android.data.repository

import com.qarena.android.core.network.RetrofitClient
import com.qarena.android.data.remote.ApiErrorParser
import com.qarena.android.data.remote.ApiException
import com.qarena.android.data.remote.api.AuthApi
import com.qarena.android.data.remote.dto.ForgotPasswordRequest
import com.qarena.android.data.remote.dto.ForgotPasswordResponse
import com.qarena.android.data.remote.dto.LoginRequest
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
import retrofit2.HttpException

class AuthRepository(
    private val authApi: AuthApi = RetrofitClient.retrofit.create(AuthApi::class.java)
) {

    suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return try {
            val response = authApi.register(request)
            val body = response.body()

            if (response.code() == 201 && body?.success == true) {
                Result.success(body)
            } else if (!response.isSuccessful) {
                val error = ApiErrorParser.parseErrorBody(response.errorBody()?.string())
                val resolvedCode = ApiErrorParser.resolvedCode(error)
                Result.failure(
                    ApiException(
                        message = ApiErrorParser.messageForCode(resolvedCode, error.message)
                            .let { fallback -> ApiErrorParser.messageForHttpStatus(response.code(), fallback) },
                        code = resolvedCode,
                        field = error.field
                    )
                )
            } else {
                Result.failure(Exception("Registration failed. Unexpected server response."))
            }
        } catch (exception: Exception) {
            Result.failure(
                ApiException(
                    message = ApiErrorParser.messageForThrowable(exception),
                    code = (exception as? ApiException)?.code,
                    field = (exception as? ApiException)?.field
                )
            )
        }
    }

    suspend fun verifyEmail(request: VerifyEmailRequest): Result<MessageResponse> {
        return try {
            val response = authApi.verifyEmail(request)
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun resendVerificationEmail(email: String): Result<MessageResponse> {
        return try {
            val request = ResendVerificationEmailRequest(email = email.trim())
            val response = authApi.resendVerificationEmail(request)
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun forgotPassword(email: String): Result<ForgotPasswordResponse> {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isBlank()) {
            return Result.failure(Exception("Email is required"))
        }

        return try {
            val request = ForgotPasswordRequest(email = trimmedEmail)
            val response = authApi.forgotPassword(request)
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun resetPassword(
        token: String,
        newPassword: String
    ): Result<ResetPasswordResponse> {
        val trimmedToken = token.trim()

        if (trimmedToken.isBlank()) {
            return Result.failure(Exception("Reset token is required"))
        }

        if (newPassword.isBlank()) {
            return Result.failure(Exception("New password is required"))
        }

        return try {
            val request = ResetPasswordRequest(
                token = trimmedToken,
                newPassword = newPassword
            )
            val response = authApi.resetPassword(request)
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun login(email: String, password: String): Result<TokenResponse> {
        return try {
            val request = LoginRequest(
                email = email.trim(),
                password = password
            )
            val response = authApi.login(request)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val error = ApiErrorParser.parseErrorBody(response.errorBody()?.string())
                val resolvedCode = ApiErrorParser.resolvedCode(error)
                val fallback = if (response.code() >= 500) {
                    "Server error. Please try again later."
                } else {
                    error.message
                }
                Result.failure(
                    ApiException(
                        message = ApiErrorParser.messageForLoginCode(resolvedCode, fallback)
                            .let { parsed -> ApiErrorParser.messageForHttpStatus(response.code(), parsed) },
                        code = resolvedCode,
                        field = error.field
                    )
                )
            }
        } catch (exception: Exception) {
            Result.failure(
                ApiException(
                    message = ApiErrorParser.messageForThrowable(exception),
                    code = (exception as? ApiException)?.code,
                    field = (exception as? ApiException)?.field
                )
            )
        }
    }

    suspend fun getMe(token: String): Result<UserResponse> {
        return getCurrentUser(token)
    }

    suspend fun getCurrentUser(token: String): Result<UserResponse> {
        return try {
            val response = authApi.getCurrentUser(
                authorization = "Bearer $token"
            )
            Result.success(response)
        } catch (exception: HttpException) {
            val error = ApiErrorParser.parseErrorBody(exception.response()?.errorBody()?.string())
            val resolvedCode = ApiErrorParser.resolvedCode(error)
            Result.failure(
                ApiException(
                    message = ApiErrorParser.messageForCode(resolvedCode, error.message)
                        .let { fallback -> ApiErrorParser.messageForHttpStatus(exception.code(), fallback) },
                    code = resolvedCode,
                    field = error.field
                )
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun updateProfile(
        token: String,
        request: ProfileUpdateRequest
    ): Result<UserResponse> {
        return try {
            val response = authApi.updateProfile(
                authorization = "Bearer $token",
                profileUpdateRequest = request
            )
            Result.success(response)
        } catch (exception: HttpException) {
            val error = ApiErrorParser.parseErrorBody(exception.response()?.errorBody()?.string())
            val resolvedCode = ApiErrorParser.resolvedCode(error)
            Result.failure(
                ApiException(
                    message = ApiErrorParser.messageForCode(resolvedCode, error.message)
                        .let { fallback -> ApiErrorParser.messageForHttpStatus(exception.code(), fallback) },
                    code = resolvedCode,
                    field = error.field
                )
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
