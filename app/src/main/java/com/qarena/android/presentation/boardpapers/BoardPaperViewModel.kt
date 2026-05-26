package com.qarena.android.presentation.boardpapers

import android.util.Log
import com.qarena.android.BuildConfig
import com.qarena.android.core.session.SessionManager
import com.qarena.android.data.remote.dto.BoardPaperDetailResponse
import com.qarena.android.data.remote.dto.BoardPaperSummary
import com.qarena.android.data.remote.dto.UserResponse
import com.qarena.android.data.repository.AuthRepository
import com.qarena.android.data.repository.BoardPaperRepository
import com.qarena.android.util.AcademicProfile
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class BoardPaperViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val boardPaperRepository: BoardPaperRepository = BoardPaperRepository()
) : ViewModel() {

    var uiState by mutableStateOf(
        BoardPaperUiState(
            selectedAcademicLevel = "",
            infoMessage = null
        )
    )
        private set

    init {
        bootstrapAcademicLevel()
    }

    fun loadAvailablePapers(academicLevel: String) {
        val resolvedAcademicLevel = academicLevel.trim().takeIf { it.isNotBlank() }

        if (resolvedAcademicLevel == null) {
            uiState = uiState.copy(
                isLoadingAvailable = false,
                isLoadingPaper = false,
                availablePapers = emptyList(),
                loadedPapers = emptyList(),
                selectedAcademicLevel = "",
                selectedSubjectCode = null,
                selectedBoardName = null,
                selectedYear = null,
                selectedPaperType = null,
                subjectOptions = emptyList(),
                boardOptions = emptyList(),
                yearOptions = emptyList(),
                paperTypeOptions = emptyList(),
                infoMessage = "Please complete your academic profile first."
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isLoadingAvailable = true,
                isLoadingPaper = false,
                errorMessage = null,
                infoMessage = null,
                loadedPapers = emptyList(),
                selectedAcademicLevel = resolvedAcademicLevel,
                selectedSubjectCode = null,
                selectedBoardName = null,
                selectedYear = null,
                selectedPaperType = null,
                subjectOptions = emptyList(),
                boardOptions = emptyList(),
                yearOptions = emptyList(),
                paperTypeOptions = emptyList()
            )

            val result = boardPaperRepository.listAvailablePapers(resolvedAcademicLevel)

            result
                .onSuccess { papers ->
                    uiState = uiState.copy(
                        isLoadingAvailable = false,
                        availablePapers = papers,
                        errorMessage = null,
                        loadedPapers = emptyList(),
                        subjectOptions = buildSubjectOptions(papers),
                        boardOptions = emptyList(),
                        yearOptions = emptyList(),
                        paperTypeOptions = emptyList()
                    )
                }
                .onFailure { exception ->
                    uiState = uiState.copy(
                        isLoadingAvailable = false,
                        availablePapers = emptyList(),
                        loadedPapers = emptyList(),
                        errorMessage = exception.message ?: "Failed to load board papers.",
                        subjectOptions = emptyList(),
                        boardOptions = emptyList(),
                        yearOptions = emptyList(),
                        paperTypeOptions = emptyList()
                    )
                }
        }
    }

    fun selectAcademicLevel(academicLevel: String) {
        val resolvedAcademicLevel = academicLevel.trim()
        if (resolvedAcademicLevel == uiState.selectedAcademicLevel) {
            return
        }

        loadAvailablePapers(resolvedAcademicLevel)
    }

    fun selectSubjectCode(subjectCode: String) {
        val resolvedSubjectCode = subjectCode.trim().takeIf { it.isNotBlank() }
        val newBoardOptions = buildBoardOptions(uiState.availablePapers, resolvedSubjectCode)

        uiState = uiState.copy(
            selectedSubjectCode = resolvedSubjectCode,
            selectedBoardName = null,
            selectedYear = null,
            selectedPaperType = null,
            loadedPapers = emptyList(),
            errorMessage = null,
            boardOptions = newBoardOptions,
            yearOptions = emptyList(),
            paperTypeOptions = emptyList()
        )
    }

    fun selectBoardName(boardName: String) {
        val resolvedBoardName = boardName.trim().takeIf { it.isNotBlank() }
        val newYearOptions = buildYearOptions(uiState.availablePapers, uiState.selectedSubjectCode, resolvedBoardName)

        uiState = uiState.copy(
            selectedBoardName = resolvedBoardName,
            selectedYear = null,
            selectedPaperType = null,
            loadedPapers = emptyList(),
            errorMessage = null,
            yearOptions = newYearOptions,
            paperTypeOptions = emptyList()
        )
    }

    fun selectYear(year: Int) {
        val newPaperTypeOptions = buildPaperTypeOptions(
            availablePapers = uiState.availablePapers,
            selectedSubjectCode = uiState.selectedSubjectCode,
            selectedBoardName = uiState.selectedBoardName,
            selectedYear = year
        )

        uiState = uiState.copy(
            selectedYear = year,
            selectedPaperType = null,
            loadedPapers = emptyList(),
            errorMessage = null,
            paperTypeOptions = newPaperTypeOptions
        )
    }

    fun selectPaperType(paperType: String) {
        uiState = uiState.copy(
            selectedPaperType = paperType.trim().takeIf { it.isNotBlank() },
            loadedPapers = emptyList(),
            errorMessage = null
        )
    }

    fun viewPaper() {
        Log.d("BoardPapers", "selectedAcademicLevel=${uiState.selectedAcademicLevel}")
        Log.d("BoardPapers", "selectedSubjectCode=${uiState.selectedSubjectCode}")
        Log.d("BoardPapers", "selectedBoardName=${uiState.selectedBoardName}")
        Log.d("BoardPapers", "selectedYear=${uiState.selectedYear}")
        Log.d("BoardPapers", "selectedPaperType=${uiState.selectedPaperType}")

        val matchingPapers = uiState.availablePapers.filter { paper ->
            paper.academicLevel.equals(uiState.selectedAcademicLevel, ignoreCase = true) &&
                paper.subjectCode == uiState.selectedSubjectCode &&
                paper.boardName == uiState.selectedBoardName &&
                paper.examYear == uiState.selectedYear
        }

        Log.d("BoardPapers", "matchingPapers=$matchingPapers")

        var targetPapers = matchingPapers

        if (uiState.selectedPaperType != "FULL") {
            targetPapers = matchingPapers.filter { paper ->
                paper.paperType.equals(uiState.selectedPaperType, ignoreCase = true)
            }
        }

        if (targetPapers.isEmpty()) {
            uiState = uiState.copy(
                errorMessage = "No paper found for this board, year, and subject.",
                loadedPapers = emptyList()
            )
            return
        }

        Log.d("BoardPapers", "targetPapers=$targetPapers")
        Log.d("BoardPapers", "loadingExamIds=${targetPapers.map { it.examId }}")

        viewModelScope.launch {
            uiState = uiState.copy(
                isLoadingPaper = true,
                errorMessage = null,
                loadedPapers = emptyList()
            )

            val details = supervisorScope {
                targetPapers.mapNotNull { paper ->
                    val examId = paper.examId ?: return@mapNotNull null
                    async {
                        boardPaperRepository.getPaperById(examId)
                            .getOrNull()
                    }
                }.awaitAll().filterNotNull()
            }

            if (details.isEmpty()) {
                uiState = uiState.copy(
                    isLoadingPaper = false,
                    errorMessage = "Failed to load selected paper.",
                    loadedPapers = emptyList()
                )
            } else {
                uiState = uiState.copy(
                    isLoadingPaper = false,
                    loadedPapers = details,
                    errorMessage = null
                )
            }
        }
    }

    private fun bootstrapAcademicLevel() {
        viewModelScope.launch {
            val resolvedAcademicLevel = resolveAcademicLevelFromSources()

            if (resolvedAcademicLevel.isNullOrBlank()) {
                uiState = uiState.copy(
                    isLoadingAvailable = false,
                    isLoadingPaper = false,
                    availablePapers = emptyList(),
                    loadedPapers = emptyList(),
                    selectedAcademicLevel = "",
                    selectedSubjectCode = null,
                    selectedBoardName = null,
                    selectedYear = null,
                    selectedPaperType = null,
                    subjectOptions = emptyList(),
                    boardOptions = emptyList(),
                    yearOptions = emptyList(),
                    paperTypeOptions = emptyList(),
                    infoMessage = "Please complete your academic profile first."
                )
                return@launch
            }

            uiState = uiState.copy(selectedAcademicLevel = resolvedAcademicLevel, infoMessage = null)
            loadAvailablePapers(resolvedAcademicLevel)
        }
    }

    private suspend fun resolveAcademicLevelFromSources(): String? {
        val sessionAcademicLevel = SessionManager.userAcademicLevel?.trim()?.takeIf { it.isNotBlank() }
        val token = SessionManager.accessToken ?: return sessionAcademicLevel

        val profileResult = authRepository.getCurrentUser(token)
        profileResult.onSuccess { user ->
            val resolvedFromProfile = resolveAcademicLevelFromProfile(user)
            if (!resolvedFromProfile.isNullOrBlank()) {
                SessionManager.saveSession(
                    token = token,
                    email = user.email,
                    role = user.role,
                    userId = user.id,
                    academicLevel = resolvedFromProfile,
                    universityId = user.universityId,
                    departmentId = user.departmentId,
                    curriculum = user.curriculum,
                    streamGroup = user.streamGroup
                )
                return resolvedFromProfile
            }
        }

        if (!sessionAcademicLevel.isNullOrBlank()) {
            return sessionAcademicLevel
        }

        if (BuildConfig.DEBUG) {
            // DEVELOPMENT ONLY: remove this temporary fallback before production release.
            return "SSC"
        }

        return null
    }

    private fun resolveAcademicLevelFromProfile(user: UserResponse): String? {
        val userAcademicLevel = user.academicLevel?.trim()?.takeIf { it.isNotBlank() }
        if (!userAcademicLevel.isNullOrBlank()) {
            return userAcademicLevel
        }

        return AcademicProfile.resolveAcademicLevel(user).trim().takeIf { it.isNotBlank() }
    }

    private fun buildAcademicLevelOptions(availablePapers: List<BoardPaperSummary>): List<FilterOption<String>> {
        return availablePapers
            .distinctBy { it.academicLevel }
            .mapNotNull { paper ->
                val academicLevel = paper.academicLevel?.trim()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                FilterOption(label = academicLevel, value = academicLevel)
            }
    }

    private fun buildSubjectOptions(availablePapers: List<BoardPaperSummary>): List<FilterOption<String>> {
        return availablePapers
            .distinctBy { it.subjectCode }
            .mapNotNull { paper ->
                val code = paper.subjectCode?.trim()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val name = paper.subjectName?.trim()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                FilterOption(label = "$name ($code)", value = code)
            }
            .filter { it.value.isNotBlank() }
    }

    private fun buildBoardOptions(
        availablePapers: List<BoardPaperSummary>,
        selectedSubjectCode: String?
    ): List<FilterOption<String>> {
        return availablePapers
            .filter { it.subjectCode == selectedSubjectCode }
            .distinctBy { it.boardName }
            .mapNotNull { paper ->
                val boardName = paper.boardName?.trim()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                FilterOption(label = "$boardName Board", value = boardName)
            }
            .filter { it.value.isNotBlank() }
    }

    private fun buildYearOptions(
        availablePapers: List<BoardPaperSummary>,
        selectedSubjectCode: String?,
        selectedBoardName: String?
    ): List<FilterOption<Int>> {
        return availablePapers
            .filter {
                it.subjectCode == selectedSubjectCode &&
                    it.boardName == selectedBoardName
            }
            .distinctBy { it.examYear }
            .mapNotNull { paper ->
                val year = paper.examYear ?: return@mapNotNull null
                if (year <= 0) return@mapNotNull null
                FilterOption(label = year.toString(), value = year)
            }
    }

    private fun buildPaperTypeOptions(
        availablePapers: List<BoardPaperSummary>,
        selectedSubjectCode: String?,
        selectedBoardName: String?,
        selectedYear: Int?
    ): List<FilterOption<String>> {
        val matchingPapers = availablePapers.filter {
            it.subjectCode == selectedSubjectCode &&
                it.boardName == selectedBoardName &&
                it.examYear == selectedYear
        }

        val paperTypes = matchingPapers
            .mapNotNull { paper -> paper.paperType?.trim()?.takeIf { it.isNotBlank() } }
            .distinct()
            .map { paperType -> FilterOption(label = paperType, value = paperType) }

        return if (paperTypes.size > 1) {
            listOf(FilterOption(label = "Full Paper", value = "FULL")) + paperTypes
        } else {
            paperTypes
        }
    }
}