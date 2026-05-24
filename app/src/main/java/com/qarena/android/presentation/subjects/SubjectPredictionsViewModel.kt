package com.qarena.android.presentation.subjects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.data.repository.SubjectRepository
import com.qarena.android.model.Suggestion
import com.qarena.android.model.Subject
import com.qarena.android.util.PaperTypeLookups
import com.qarena.android.util.SubjectLookups
import kotlinx.coroutines.launch

sealed interface SubjectPredictionsUiState {
    data object Idle : SubjectPredictionsUiState
    data object Loading : SubjectPredictionsUiState
    data class Success(val predictions: List<Suggestion>) : SubjectPredictionsUiState
    data class Error(val message: String) : SubjectPredictionsUiState
}

class SubjectPredictionsViewModel : ViewModel() {

    private val subjectRepository = SubjectRepository()

    var predictionsState by mutableStateOf<SubjectPredictionsUiState>(SubjectPredictionsUiState.Idle)
        private set

    var subject by mutableStateOf<Subject?>(null)
        private set

    var supportedPaperTypes by mutableStateOf<List<String>>(emptyList())
        private set

    var selectedPaperType by mutableStateOf<String?>(null)
        private set

    fun loadSubjectPredictions(subjectCode: String, paperType: String? = null) {
        viewModelScope.launch {
            val trimmedSubjectCode = subjectCode.trim()

            if (trimmedSubjectCode.isBlank()) {
                predictionsState = SubjectPredictionsUiState.Error("Subject code is required")
                return@launch
            }

            predictionsState = SubjectPredictionsUiState.Loading

            loadSubjectMetadata(trimmedSubjectCode)

            val effectivePaperType = PaperTypeLookups.resolveSelectedPaperType(
                preferredPaperType = paperType ?: selectedPaperType,
                supportedPaperTypes = supportedPaperTypes
            )
            selectedPaperType = effectivePaperType

            val result = subjectRepository.getSubjectPredictions(
                subjectCode = trimmedSubjectCode,
                paperType = effectivePaperType,
                debugScreenName = "SubjectPredictions",
                subject = subject
            )

            result
                .onSuccess { predictions ->
                    predictionsState = SubjectPredictionsUiState.Success(predictions)
                }
                .onFailure { exception ->
                    predictionsState = SubjectPredictionsUiState.Error(
                        exception.message ?: "Failed to load subject predictions"
                    )
                }
        }
    }

    fun selectPaperType(subjectCode: String, paperType: String) {
        loadSubjectPredictions(subjectCode, paperType)
    }

    private suspend fun loadSubjectMetadata(subjectCode: String) {
        if (subject != null && subject?.subjectCode == subjectCode) {
            return
        }

        val result = subjectRepository.getSubjectOverview(
            subjectCode = subjectCode,
            debugScreenName = "SubjectPredictions",
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
