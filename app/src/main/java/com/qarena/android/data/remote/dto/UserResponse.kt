package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val id: Int? = null,
    val email: String? = null,
    val role: String? = null,

    @SerializedName("full_name")
    val fullName: String? = null,

    @SerializedName("university_id")
    val universityId: Int? = null,

    @SerializedName("department_id")
    val departmentId: Int? = null,

    @SerializedName("is_email_verified")
    val isEmailVerified: Boolean? = null
)
