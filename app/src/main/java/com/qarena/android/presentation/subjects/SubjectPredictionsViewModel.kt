package com.qarena.android.presentation.subjects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.data.repository.SubjectRepository
import com.qarena.android.model.Suggestion
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

    fun loadSubjectPredictions(subjectCode: String) {
        viewModelScope.launch {
            val trimmedSubjectCode = subjectCode.trim()

            if (trimmedSubjectCode.isBlank()) {
                predictionsState = SubjectPredictionsUiState.Error("Subject code is required")
                return@launch
            }

            predictionsState = SubjectPredictionsUiState.Loading

            val result = subjectRepository.getSubjectPredictions(trimmedSubjectCode)

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
}
