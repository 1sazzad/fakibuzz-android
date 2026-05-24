package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SubjectDto(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("subject_code")
    val subject_code: String? = null,

    @SerializedName("subject_name")
    val subject_name: String? = null,

    @SerializedName("university_id")
    val university_id: Int? = null,

    @SerializedName("department_id")
    val department_id: Int? = null,

    @SerializedName("academic_level")
    val academic_level: String? = null,

    @SerializedName("group")
    val group: String? = null,

    @SerializedName("supported_paper_types")
    val supported_paper_types: List<String>? = null,

    @SerializedName("status")
    val status: String? = null
)
