package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SubjectListResponse(
    val success: Boolean? = null,
    @SerializedName("subjects")
    val subjects: List<SubjectDto>? = null,

    @SerializedName("data")
    val data: List<SubjectDto>? = null,

    @SerializedName("results")
    val results: List<SubjectDto>? = null,

    @SerializedName("items")
    val items: List<SubjectDto>? = null,

    val total: Int? = null,

    @SerializedName("status_filter")
    val statusFilter: String? = null
)

fun SubjectListResponse.resolvedSubjects(): List<SubjectDto> {
    return subjects ?: data ?: results ?: items ?: emptyList()
}
