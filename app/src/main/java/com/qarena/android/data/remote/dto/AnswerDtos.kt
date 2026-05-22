package com.qarena.android.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class GenerateAnswerRequest(
    @SerializedName("question_id")
    val questionId: Int? = null,

    @SerializedName("question_text")
    val questionText: String? = null,

    @SerializedName("subject_code")
    val subjectCode: String? = null,

    val prompt: String? = null
)

data class GenerateAnswerResponse(
    val answer: String? = null,

    @SerializedName("job_id")
    val jobId: String? = null,

    val status: String? = null,
    val message: String? = null
)

data class JobResponse(
    @SerializedName("job_id")
    val jobId: String? = null,

    val status: String? = null,
    val message: String? = null,
    val result: JsonElement? = null,
    val answer: String? = null,
    val error: String? = null,
    val progress: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)
