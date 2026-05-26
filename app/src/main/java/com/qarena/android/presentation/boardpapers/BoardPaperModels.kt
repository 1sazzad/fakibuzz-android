package com.qarena.android.presentation.boardpapers

import com.qarena.android.data.remote.dto.BoardPaperDetailResponse
import com.qarena.android.data.remote.dto.BoardPaperSummary

data class FilterOption<T>(
    val label: String,
    val value: T
)

data class BoardPaperUiState(
    val isLoadingAvailable: Boolean = false,
    val isLoadingPaper: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val availablePapers: List<BoardPaperSummary> = emptyList(),
    val loadedPapers: List<BoardPaperDetailResponse> = emptyList(),
    val selectedAcademicLevel: String = "",
    val selectedSubjectCode: String? = null,
    val selectedBoardName: String? = null,
    val selectedYear: Int? = null,
    val selectedPaperType: String? = null,
    val subjectOptions: List<FilterOption<String>> = emptyList(),
    val boardOptions: List<FilterOption<String>> = emptyList(),
    val yearOptions: List<FilterOption<Int>> = emptyList(),
    val paperTypeOptions: List<FilterOption<String>> = emptyList()
)