package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SubjectListResponse(
    // New hardened API prefers `items`.
    val success: Boolean? = null,
    val items: List<SubjectDto>? = null,

    // Legacy field kept for temporary fallback.
    val subjects: List<SubjectDto>? = null,

    val total: Int? = null,

    @SerializedName("status_filter")
    val statusFilter: String? = null
)
