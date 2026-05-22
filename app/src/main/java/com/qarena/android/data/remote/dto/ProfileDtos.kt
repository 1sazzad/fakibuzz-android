package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProfileUpdateRequest(
    @SerializedName("full_name")
    val fullName: String? = null,

    @SerializedName("academic_level")
    val academicLevel: String? = null,

    @SerializedName("institution_type")
    val institutionType: String? = null,

    @SerializedName("curriculum")
    val curriculum: String? = null,

    @SerializedName("stream_group")
    val streamGroup: String? = null,

    @SerializedName("class_level")
    val classLevel: String? = null,

    @SerializedName("university_id")
    val universityId: Int? = null,

    @SerializedName("department_id")
    val departmentId: Int? = null,

    val program: String? = null,

    @SerializedName("batch_session")
    val batchSession: String? = null
)
