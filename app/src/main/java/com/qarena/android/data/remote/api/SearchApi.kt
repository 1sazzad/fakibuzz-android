package com.qarena.android.data.remote.api

import com.qarena.android.data.remote.dto.SearchRequest
import com.qarena.android.data.remote.dto.SearchResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SearchApi {

    @POST("search")
    suspend fun search(
        @Header("Authorization") authorization: String,
        @Body searchRequest: SearchRequest
    ): SearchResponse
}
