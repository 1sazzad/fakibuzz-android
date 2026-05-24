package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val role: String,

    @SerializedName("is_email_verified")
    val isEmailVerified: Boolean? = null,

    @SerializedName("academic_level")
    val academicLevel: String? = null,

    @SerializedName("university_id")
    val universityId: Int? = null,

    @SerializedName("department_id")
    val departmentId: Int? = null
)
