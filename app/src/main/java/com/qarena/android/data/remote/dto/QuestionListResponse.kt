package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class QuestionListResponse(
    @SerializedName("subject_id")
    val subjectId: Int? = null,

    @SerializedName("subject_code")
    val subjectCode: String? = null,

    val questions: List<QuestionResponse>? = emptyList(),
    val total: Int? = null,

    @SerializedName("total_pages")
    val totalPages: Int? = null,

    @SerializedName("current_page")
    val currentPage: Int? = null,

    val page: Int? = null,
    val limit: Int? = null
)
