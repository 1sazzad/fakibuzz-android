package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SearchRequest(
    val query: String,

    @SerializedName("subject_code")
    val subjectCode: String? = null,

    @SerializedName("top_k")
    val topK: Int? = null
)

data class SearchResponse(
    val results: List<SearchResultResponse>? = null,
    val questions: List<SearchResultResponse>? = null,
    val message: String? = null,
    val status: String? = null
)

data class SearchResultResponse(
    @SerializedName("question_id")
    val questionId: Int? = null,

    @SerializedName("question_text")
    val questionText: String? = null,

    @SerializedName("subject_code")
    val subjectCode: String? = null,

    @SerializedName("subject_name")
    val subjectName: String? = null,

    val topic: String? = null,
    val marks: Int? = null,
    val score: Double? = null,

    @SerializedName("similarity_score")
    val similarityScore: Double? = null,

    val year: Int? = null,

    @SerializedName("exam_year")
    val examYear: Int? = null
)
