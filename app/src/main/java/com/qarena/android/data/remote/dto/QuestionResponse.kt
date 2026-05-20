package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class QuestionResponse(
    val id: Int? = null,

    @SerializedName("question_no")
    val questionNo: String? = null,

    @SerializedName("question_text")
    val questionText: String? = null,

    val marks: Int? = null,
    val topic: String? = null,

    @SerializedName("exam_year")
    val examYear: Int? = null,

    @SerializedName("subject_code")
    val subjectCode: String? = null
)
