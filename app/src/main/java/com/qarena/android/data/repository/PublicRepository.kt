package com.qarena.android.data.repository

import com.qarena.android.core.network.RetrofitClient
import com.qarena.android.data.remote.api.PublicApi
import com.qarena.android.data.remote.dto.DepartmentListResponse
import com.qarena.android.data.remote.dto.HealthResponse
import com.qarena.android.data.remote.dto.UniversityListResponse

class PublicRepository(
    private val publicApi: PublicApi = RetrofitClient.retrofit.create(PublicApi::class.java)
) {
    private val institutionRepository = InstitutionRepository(publicApi)

    suspend fun getUniversities(): Result<UniversityListResponse> {
        return institutionRepository.getUniversities()
    }

    suspend fun getDepartments(universityId: Int): Result<DepartmentListResponse> {
        return institutionRepository.getDepartments(universityId)
    }

    suspend fun getHealth(): Result<HealthResponse> {
        return try {
            val response = publicApi.getHealth()
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
