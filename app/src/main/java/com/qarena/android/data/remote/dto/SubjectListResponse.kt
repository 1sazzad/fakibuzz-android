package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SubjectListResponse(
    val subjects: List<SubjectResponse> = emptyList(),
    val total: Int? = null,

    @SerializedName("status_filter")
    val statusFilter: String? = null
)
