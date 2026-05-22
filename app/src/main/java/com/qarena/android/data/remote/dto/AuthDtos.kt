package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val full_name: String,
    val email: String,
    val phone_number: String,
    val password: String,
    val academic_level: String? = null,
    val institution_type: String? = null,
    val curriculum: String? = null,
    val stream_group: String? = null,
    val class_level: String? = null,
    val university_id: Int? = null,
    val department_id: Int? = null,
    val program: String? = null,
    val batch_session: String? = null,
    val terms_accepted: Boolean
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val user: RegisterUser
)

data class RegisterUser(
    val id: Int,
    val full_name: String,
    val email: String,
    val role: String,
    val is_email_verified: Boolean
)

data class ApiErrorResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val field: String? = null,
    val code: String? = null
)

data class VerifyEmailRequest(
    val email: String? = null,
    val code: String? = null,
    val token: String? = null
)

data class ResendVerificationEmailRequest(
    val email: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val message: String? = null,
    val detail: String? = null,
    val success: Boolean? = null
)

data class ResetPasswordRequest(
    val token: String,

    @SerializedName("new_password")
    val newPassword: String
)

data class ResetPasswordResponse(
    val message: String? = null,
    val detail: String? = null,
    val success: Boolean? = null
)

data class MessageResponse(
    val message: String? = null,
    val detail: String? = null,
    val success: Boolean? = null
)
