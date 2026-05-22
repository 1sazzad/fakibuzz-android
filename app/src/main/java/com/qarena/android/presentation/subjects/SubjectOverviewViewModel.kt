package com.qarena.android.presentation.subjects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.data.remote.dto.SubjectOverviewResponse
import com.qarena.android.data.repository.SubjectRepository
import kotlinx.coroutines.launch

sealed interface SubjectOverviewUiState {
    data object Idle : SubjectOverviewUiState
    data object Loading : SubjectOverviewUiState
    data class Success(val overview: SubjectOverviewResponse) : SubjectOverviewUiState
    data class Error(val message: String) : SubjectOverviewUiState
}

class SubjectOverviewViewModel : ViewModel() {

    private val subjectRepository = SubjectRepository()

    var overviewState by mutableStateOf<SubjectOverviewUiState>(SubjectOverviewUiState.Idle)
        private set

    fun loadSubjectOverview(subjectCode: String) {
        viewModelScope.launch {
            val trimmedSubjectCode = subjectCode.trim()

            if (trimmedSubjectCode.isBlank()) {
                overviewState = SubjectOverviewUiState.Error("Subject code is required")
                return@launch
            }

            overviewState = SubjectOverviewUiState.Loading

            val result = subjectRepository.getSubjectOverview(trimmedSubjectCode)

            result
                .onSuccess { overview ->
                    overviewState = SubjectOverviewUiState.Success(overview)
                }
                .onFailure { exception ->
                    overviewState = SubjectOverviewUiState.Error(
                        exception.message ?: "Failed to load subject overview"
                    )
                }
        }
    }
}
