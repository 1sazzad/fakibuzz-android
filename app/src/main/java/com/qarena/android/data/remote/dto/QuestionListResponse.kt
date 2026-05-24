package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class QuestionListResponse(
    @SerializedName("items")
    val items: List<QuestionDto> = emptyList(),

    @SerializedName("questions")
    val questions: List<QuestionDto> = emptyList(),

    @SerializedName("results")
    val results: List<QuestionDto> = emptyList(),

    @SerializedName("data")
    val data: List<QuestionDto> = emptyList(),

    @SerializedName("total")
    val total: Int? = null,

    @SerializedName("count")
    val count: Int? = null
) {
    fun questionList(): List<QuestionDto> {
        return when {
            items.isNotEmpty() -> items
            questions.isNotEmpty() -> questions
            results.isNotEmpty() -> results
            data.isNotEmpty() -> data
            else -> emptyList()
        }
    }

    fun totalCount(): Int {
        return total ?: count ?: questionList().size
    }
}