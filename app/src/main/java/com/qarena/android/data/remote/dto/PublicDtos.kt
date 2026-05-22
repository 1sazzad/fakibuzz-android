package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.qarena.android.model.Department
import com.qarena.android.model.University

data class LookupListResponse<T>(
    val success: Boolean? = null,
    val items: List<T>? = null
)

data class UniversityLookupResponse(
    val success: Boolean? = null,
    val items: List<UniversityDto>? = null
)

data class DepartmentLookupResponse(
    val success: Boolean? = null,
    val items: List<DepartmentDto>? = null
)

data class UniversityDto(
    val id: Int,
    val name: String,

    @SerializedName("short_name")
    val short_name: String? = null
)

data class DepartmentDto(
    val id: Int,
    val name: String,

    @SerializedName("short_name")
    val short_name: String? = null,

    @SerializedName("university_id")
    val university_id: Int
)

data class UniversityListResponse(
    val universities: List<University> = emptyList(),
    val total: Int? = null
)

data class DepartmentListResponse(
    val departments: List<Department> = emptyList(),
    val total: Int? = null,

    @SerializedName("university_id")
    val universityId: Int? = null
)

data class HealthResponse(
    val status: String? = null,
    val message: String? = null,
    val version: String? = null,
    val timestamp: String? = null
)
