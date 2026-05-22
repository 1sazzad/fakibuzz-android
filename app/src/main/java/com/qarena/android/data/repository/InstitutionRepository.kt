package com.qarena.android.data.repository

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.qarena.android.core.network.RetrofitClient
import com.qarena.android.data.remote.api.PublicApi
import com.qarena.android.data.remote.dto.DepartmentDto
import com.qarena.android.data.remote.dto.DepartmentListResponse
import com.qarena.android.data.remote.dto.DepartmentLookupResponse
import com.qarena.android.data.remote.dto.UniversityDto
import com.qarena.android.data.remote.dto.UniversityListResponse
import com.qarena.android.data.remote.dto.UniversityLookupResponse
import com.qarena.android.model.Department
import com.qarena.android.model.University

class InstitutionRepository(
    private val publicApi: PublicApi = RetrofitClient.retrofit.create(PublicApi::class.java)
) {
    private val gson = Gson()

    suspend fun getUniversities(): Result<UniversityListResponse> {
        return try {
            Result.success(parseUniversities(publicApi.getUniversities()))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getDepartments(universityId: Int): Result<DepartmentListResponse> {
        if (universityId <= 0) {
            return Result.failure(Exception("University is required"))
        }

        return try {
            Result.success(parseDepartments(publicApi.getDepartments(universityId)))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun parseUniversities(json: JsonElement): UniversityListResponse {
        val universities = universityDtos(json).mapNotNull { it.toUniversityOrNull() }

        return UniversityListResponse(universities = universities)
    }

    fun parseDepartments(json: JsonElement): DepartmentListResponse {
        val departments = departmentDtos(json).mapNotNull { it.toDepartmentOrNull() }

        return DepartmentListResponse(departments = departments)
    }

    private fun universityDtos(json: JsonElement): List<UniversityDto> {
        return when {
            json.isJsonArray -> json.asJsonArray.map {
                gson.fromJson(it, UniversityDto::class.java)
            }
            json.isJsonObject -> {
                val body = json.asJsonObject
                val newResponse = gson.fromJson(body, UniversityLookupResponse::class.java)
                val list = newResponse.items
                    ?: body.getAsJsonArray("universities")?.map {
                        gson.fromJson(it, UniversityDto::class.java)
                    }
                list.orEmpty()
            }
            else -> emptyList()
        }
    }

    private fun departmentDtos(json: JsonElement): List<DepartmentDto> {
        return when {
            json.isJsonArray -> json.asJsonArray.map {
                gson.fromJson(it, DepartmentDto::class.java)
            }
            json.isJsonObject -> {
                val body = json.asJsonObject
                val newResponse = gson.fromJson(body, DepartmentLookupResponse::class.java)
                val list = newResponse.items
                    ?: body.getAsJsonArray("departments")?.map {
                        gson.fromJson(it, DepartmentDto::class.java)
                    }
                list.orEmpty()
            }
            else -> emptyList()
        }
    }

    private fun UniversityDto.toUniversityOrNull(): University? {
        val trimmedName = name.trim()
        if (id <= 0 || trimmedName.isBlank()) {
            return null
        }

        return University(
            id = id,
            name = trimmedName,
            shortName = short_name?.trim()?.takeIf { it.isNotBlank() }
        )
    }

    private fun DepartmentDto.toDepartmentOrNull(): Department? {
        val trimmedName = name.trim()
        if (id <= 0 || university_id <= 0 || trimmedName.isBlank()) {
            return null
        }

        return Department(
            id = id,
            name = trimmedName,
            shortName = short_name?.trim()?.takeIf { it.isNotBlank() },
            universityId = university_id
        )
    }
}
