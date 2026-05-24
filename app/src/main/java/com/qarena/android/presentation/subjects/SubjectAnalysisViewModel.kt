package com.qarena.android.presentation.subjects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.data.remote.dto.SubjectAnalysisResponse
import com.qarena.android.data.repository.SubjectRepository
import com.qarena.android.model.Subject
import com.qarena.android.util.PaperTypeLookups
import com.qarena.android.util.SubjectLookups
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

    var subject by mutableStateOf<Subject?>(null)
        private set

    var supportedPaperTypes by mutableStateOf<List<String>>(emptyList())
        private set

    var selectedPaperType by mutableStateOf<String?>(null)
        private set

    fun loadSubjectAnalysis(subjectCode: String, paperType: String? = null) {
        viewModelScope.launch {
            val trimmedSubjectCode = subjectCode.trim()

            if (trimmedSubjectCode.isBlank()) {
                analysisState = SubjectAnalysisUiState.Error("Subject code is required")
                return@launch
            }

            analysisState = SubjectAnalysisUiState.Loading

            loadSubjectMetadata(trimmedSubjectCode)

            val effectivePaperType = PaperTypeLookups.resolveSelectedPaperType(
                preferredPaperType = paperType ?: selectedPaperType,
                supportedPaperTypes = supportedPaperTypes
            )
            selectedPaperType = effectivePaperType

            val result = subjectRepository.getSubjectAnalysis(
                subjectCode = trimmedSubjectCode,
                paperType = effectivePaperType,
                debugScreenName = "SubjectAnalysis",
                subject = subject
            )

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

    fun selectPaperType(subjectCode: String, paperType: String) {
        loadSubjectAnalysis(subjectCode, paperType)
    }

    private suspend fun loadSubjectMetadata(subjectCode: String) {
        if (subject != null && subject?.subjectCode == subjectCode) {
            return
        }

        val result = subjectRepository.getSubjectOverview(
            subjectCode = subjectCode,
            debugScreenName = "SubjectAnalysis",
            subject = subject
        )

        result
            .onSuccess { overview ->
                val subjectMetadata = overview.subject?.let { with(SubjectLookups) { it.toSubject() } }
                subject = subjectMetadata
                supportedPaperTypes = PaperTypeLookups.normalizeSupportedPaperTypes(
                    subjectMetadata?.supportedPaperTypes ?: overview.subject?.supportedPaperTypes
                )
                selectedPaperType = PaperTypeLookups.resolveSelectedPaperType(
                    preferredPaperType = selectedPaperType,
                    supportedPaperTypes = supportedPaperTypes
                )
            }
            .onFailure {
                if (subject == null) {
                    supportedPaperTypes = emptyList()
                    selectedPaperType = null
                }
            }
    }
}
