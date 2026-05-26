package com.qarena.android.data.remote.api

import com.qarena.android.data.remote.dto.BoardPaperDetailResponse
import com.qarena.android.data.remote.dto.BoardPaperListResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface BoardPaperApi {

    @GET("papers/board")
    suspend fun listBoardPapers(
        @Header("Authorization") authorization: String,
        @Query("academic_level") academicLevel: String,
        @Query("subject_code") subjectCode: String? = null,
        @Query("board_name") boardName: String? = null,
        @Query("exam_year") examYear: Int? = null,
        @Query("paper_type") paperType: String? = null,
        @Query("group") group: String? = null
    ): BoardPaperListResponse

    @GET("papers/board/{examId}")
    suspend fun getBoardPaperById(
        @Header("Authorization") authorization: String,
        @Path("examId") examId: Int
    ): BoardPaperDetailResponse
}