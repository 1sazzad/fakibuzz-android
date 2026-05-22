package com.qarena.android.data.remote.dto

data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val role: String
)
