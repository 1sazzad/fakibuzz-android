package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SubjectResponse(
    @SerializedName(value = "id", alternate = ["subject_id"])
    val id: Int? = null,

    @SerializedName("subject_code")
    val subjectCode: String? = null,

    @SerializedName("subject_name")
    val subjectName: String? = null,

    @SerializedName("university_id")
    val universityId: Int? = null,

    @SerializedName("department_id")
    val departmentId: Int? = null,

    @SerializedName("academic_level")
    val academicLevel: String? = null,

    @SerializedName("group")
    val group: String? = null,

    @SerializedName("supported_paper_types")
    val supportedPaperTypes: List<String>? = null,

    val status: String? = null
)
