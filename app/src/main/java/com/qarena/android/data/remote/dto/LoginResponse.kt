package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String? = null,

    @SerializedName("token_type")
    val tokenType: String? = null,

    val role: String? = null,
    val email: String? = null,

    @SerializedName("user_id")
    val userId: Int? = null
)
