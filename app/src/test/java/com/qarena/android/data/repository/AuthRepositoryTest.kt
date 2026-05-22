package com.qarena.android.data.repository

import com.qarena.android.data.remote.api.AuthApi
import com.qarena.android.data.remote.dto.ForgotPasswordRequest
import com.qarena.android.data.remote.dto.ForgotPasswordResponse
import com.qarena.android.data.remote.dto.LoginRequest
import com.qarena.android.data.remote.dto.MessageResponse
import com.qarena.android.data.remote.dto.ProfileUpdateRequest
import com.qarena.android.data.remote.dto.RegisterRequest
import com.qarena.android.data.remote.dto.RegisterResponse
import com.qarena.android.data.remote.dto.RegisterUser
import com.qarena.android.data.remote.dto.ResendVerificationEmailRequest
import com.qarena.android.data.remote.dto.ResetPasswordRequest
import com.qarena.android.data.remote.dto.ResetPasswordResponse
import com.qarena.android.data.remote.dto.TokenResponse
import com.qarena.android.data.remote.dto.UserResponse
import com.qarena.android.data.remote.dto.VerifyEmailRequest
import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AuthRepositoryTest {

    @Test
    fun registerReturnsSuccessOnlyForCreatedSuccessfulResponse() = runBlocking {
        val repository = AuthRepository(
            FakeAuthApi(
                Response.success(
                    201,
                    RegisterResponse(
                        success = true,
                        message = "Created",
                        user = RegisterUser(
                            id = 1,
                            full_name = "Student User",
                            email = "student@example.com",
                            role = "student",
                            is_email_verified = false
                        )
                    )
                )
            )
        )

        val result = repository.register(validRequest())

        assertTrue(result.isSuccess)
        assertEquals("Created", result.getOrThrow().message)
    }

    @Test
    fun registerParsesBackendErrorResponse() = runBlocking {
        val errorBody = """
            {
              "success": false,
              "message": "Email already exists.",
              "field": "email",
              "code": "EMAIL_ALREADY_EXISTS"
            }
        """.trimIndent().toResponseBody("application/json".toMediaType())
        val repository = AuthRepository(FakeAuthApi(Response.error(409, errorBody)))

        val result = repository.register(validRequest())

        assertTrue(result.isFailure)
        assertEquals("Email already exists.", result.exceptionOrNull()?.message)
    }

    @Test
    fun loginTrimsEmailButDoesNotTrimPassword() = runBlocking {
        val fakeApi = FakeAuthApi(
            loginResponse = Response.success(
                TokenResponse(
                    access_token = "token",
                    token_type = "bearer",
                    role = "student"
                )
            )
        )
        val repository = AuthRepository(fakeApi)

        val result = repository.login(
            email = " student@example.com ",
            password = " Password1 "
        )

        assertTrue(result.isSuccess)
        assertEquals("student@example.com", fakeApi.lastLoginRequest?.email)
        assertEquals(" Password1 ", fakeApi.lastLoginRequest?.password)
    }

    @Test
    fun loginParsesInvalidCredentialsError() = runBlocking {
        val result = loginWithErrorCode(
            code = "INVALID_CREDENTIALS",
            message = "Invalid email or password."
        )

        assertTrue(result.isFailure)
        assertEquals("Invalid email or password.", result.exceptionOrNull()?.message)
    }

    @Test
    fun loginParsesInactiveAccountError() = runBlocking {
        val result = loginWithErrorCode(
            code = "ACCOUNT_INACTIVE",
            message = "Backend message should not replace this."
        )

        assertTrue(result.isFailure)
        assertEquals(
            "Your account is inactive. Please contact support.",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun loginParsesEmailNotVerifiedError() = runBlocking {
        val result = loginWithErrorCode(
            code = "EMAIL_NOT_VERIFIED",
            message = "Backend message should not replace this."
        )

        assertTrue(result.isFailure)
        assertEquals(
            "Please verify your email before logging in.",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun loginParsesRateLimitError() = runBlocking {
        val result = loginWithErrorCode(
            code = "LOGIN_RATE_LIMITED",
            message = "Backend message should not replace this."
        )

        assertTrue(result.isFailure)
        assertEquals(
            "Too many login attempts. Please try again later.",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun loginUsesBackendMessageForValidationError() = runBlocking {
        val result = loginWithErrorCode(
            code = "VALIDATION_ERROR",
            message = "Email must be valid."
        )

        assertTrue(result.isFailure)
        assertEquals("Email must be valid.", result.exceptionOrNull()?.message)
    }

    @Test
    fun loginMapsNetworkError() = runBlocking {
        val repository = AuthRepository(
            FakeAuthApi(loginException = IOException("No route to host"))
        )

        val result = repository.login("student@example.com", "Password1")

        assertTrue(result.isFailure)
        assertEquals(
            "Network error. Check your connection and try again.",
            result.exceptionOrNull()?.message
        )
    }

    private fun validRequest(): RegisterRequest {
        return RegisterRequest(
            full_name = "Student User",
            email = "student@example.com",
            phone_number = "01712345678",
            password = "Password1",
            university_id = 1,
            department_id = 2,
            program = "BSc in CSE",
            batch_session = "2021-2022",
            terms_accepted = true
        )
    }

    private suspend fun loginWithErrorCode(
        code: String,
        message: String
    ): Result<TokenResponse> {
        val errorBody = """
            {
              "success": false,
              "message": "$message",
              "field": null,
              "code": "$code"
            }
        """.trimIndent().toResponseBody("application/json".toMediaType())
        val repository = AuthRepository(
            FakeAuthApi(loginResponse = Response.error(401, errorBody))
        )

        return repository.login("student@example.com", "Password1")
    }

    private class FakeAuthApi(
        private val registerResponse: Response<RegisterResponse> = Response.success(
            201,
            RegisterResponse(
                success = true,
                message = "Created",
                user = RegisterUser(
                    id = 1,
                    full_name = "Student User",
                    email = "student@example.com",
                    role = "student",
                    is_email_verified = false
                )
            )
        ),
        private val loginResponse: Response<TokenResponse> = Response.success(
            TokenResponse(
                access_token = "token",
                token_type = "bearer",
                role = "student"
            )
        ),
        private val loginException: Exception? = null
    ) : AuthApi {
        var lastLoginRequest: LoginRequest? = null
            private set

        override suspend fun register(registerRequest: RegisterRequest): Response<RegisterResponse> {
            return registerResponse
        }

        override suspend fun verifyEmail(verifyEmailRequest: VerifyEmailRequest): MessageResponse {
            error("Not used")
        }

        override suspend fun resendVerificationEmail(
            resendVerificationEmailRequest: ResendVerificationEmailRequest
        ): MessageResponse {
            error("Not used")
        }

        override suspend fun forgotPassword(
            forgotPasswordRequest: ForgotPasswordRequest
        ): ForgotPasswordResponse {
            error("Not used")
        }

        override suspend fun resetPassword(
            resetPasswordRequest: ResetPasswordRequest
        ): ResetPasswordResponse {
            error("Not used")
        }

        override suspend fun login(loginRequest: LoginRequest): Response<TokenResponse> {
            lastLoginRequest = loginRequest
            loginException?.let { throw it }
            return loginResponse
        }

        override suspend fun getCurrentUser(authorization: String): UserResponse {
            error("Not used")
        }

        override suspend fun updateProfile(
            authorization: String,
            profileUpdateRequest: ProfileUpdateRequest
        ): UserResponse {
            error("Not used")
        }
    }
}
