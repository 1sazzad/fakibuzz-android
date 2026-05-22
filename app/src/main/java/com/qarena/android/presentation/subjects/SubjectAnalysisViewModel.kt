package com.qarena.android.presentation.subjects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.data.remote.dto.SubjectAnalysisResponse
import com.qarena.android.data.repository.SubjectRepository
import kotlinx.coroutines.launch

sealed interface SubjectAnalysisUiState {
    data object Idle : SubjectAnalysisUiState
    data object Loading : SubjectAnalysisUiState
    data class Success(val analysis: SubjectAnalysisResponse) : SubjectAnalysisUiState
    data class Error(val message: String) : SubjectAnalysisUiState
}

class SubjectAnalysisViewModel : ViewModel() {

    private val subjectRepository = SubjectRepository()

    var analysisState by mutableStateOf<SubjectAnalysisUiState>(SubjectAnalysisUiState.Idle)
        private set

    fun loadSubjectAnalysis(subjectCode: String) {
        viewModelScope.launch {
            val trimmedSubjectCode = subjectCode.trim()

            if (trimmedSubjectCode.isBlank()) {
                analysisState = SubjectAnalysisUiState.Error("Subject code is required")
                return@launch
            }

            analysisState = SubjectAnalysisUiState.Loading

            val result = subjectRepository.getSubjectAnalysis(trimmedSubjectCode)

            result
                .onSuccess { analysis ->
                    analysisState = SubjectAnalysisUiState.Success(analysis)
                }
                .onFailure { exception ->
                    analysisState = SubjectAnalysisUiState.Error(
                        exception.message ?: "Failed to load subject analysis"
                    )
                }
        }
    }
}
