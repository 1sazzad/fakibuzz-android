package com.qarena.android.data.repository

import com.qarena.android.core.network.RetrofitClient
import com.qarena.android.core.session.SessionManager
import com.qarena.android.data.remote.api.SearchApi
import com.qarena.android.data.remote.dto.SearchRequest
import com.qarena.android.data.remote.dto.SearchResponse

class SearchRepository {

    private val searchApi: SearchApi = RetrofitClient.retrofit.create(SearchApi::class.java)

    suspend fun searchQuestions(request: SearchRequest): Result<SearchResponse> {
        val token = SessionManager.accessToken
        val trimmedQuery = request.query.trim()

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedQuery.isBlank()) {
            return Result.failure(Exception("Search query is required"))
        }

        return try {
            val response = searchApi.search(
                authorization = "Bearer $token",
                searchRequest = request.copy(
                    query = trimmedQuery,
                    subjectCode = request.subjectCode?.trim()?.takeIf { it.isNotBlank() }
                )
            )
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
