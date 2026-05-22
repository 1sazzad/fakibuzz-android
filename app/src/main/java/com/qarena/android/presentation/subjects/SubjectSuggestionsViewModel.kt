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
import com.qarena.android.util.SuggestionLookups
import com.qarena.android.util.SubjectLookups
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

sealed interface SubjectSuggestionsUiState {
    data object Idle : SubjectSuggestionsUiState
    data object Loading : SubjectSuggestionsUiState
    data class Success(val suggestions: List<Suggestion>) : SubjectSuggestionsUiState
    data class Error(val message: String) : SubjectSuggestionsUiState
}

class SubjectSuggestionsViewModel : ViewModel() {

    private val subjectRepository = SubjectRepository()
    private var loadJob: Job? = null

    var query by mutableStateOf(DEFAULT_QUERY)
        private set

    var suggestionsState by mutableStateOf<SubjectSuggestionsUiState>(SubjectSuggestionsUiState.Idle)
        private set

    var subject by mutableStateOf<Subject?>(null)
        private set

    var supportedPaperTypes by mutableStateOf<List<String>>(emptyList())
        private set

    var selectedPaperType by mutableStateOf<String?>(null)
        private set

    fun onQueryChange(newQuery: String) {
        query = SuggestionLookups.clampQueryLength(newQuery)
        suggestionsState = SubjectSuggestionsUiState.Idle
    }

    fun loadSuggestions(
        subjectCode: String,
        paperType: String? = null,
        topK: Int = DEFAULT_TOP_K
    ) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val trimmedSubjectCode = subjectCode.trim()
            val normalizedQuery = SuggestionLookups.normalizeQuery(query)
            query = normalizedQuery

            if (trimmedSubjectCode.isBlank()) {
                suggestionsState = SubjectSuggestionsUiState.Error("Subject code is required")
                return@launch
            }

            if (selectedPaperType == null || trimmedSubjectCode != subject?.subjectCode) {
                loadSubjectMetadata(trimmedSubjectCode)
            }

            val effectivePaperType = PaperTypeLookups.resolveSelectedPaperType(
                preferredPaperType = paperType ?: selectedPaperType,
                supportedPaperTypes = supportedPaperTypes
            )
            selectedPaperType = effectivePaperType

            if (normalizedQuery.isBlank()) {
                suggestionsState = SubjectSuggestionsUiState.Error("Search query is required")
                return@launch
            }

            suggestionsState = SubjectSuggestionsUiState.Loading

            val result = subjectRepository.getSubjectSuggestions(
                subjectCode = trimmedSubjectCode,
                query = normalizedQuery,
                topK = topK,
                paperType = effectivePaperType
            )

            result
                .onSuccess { suggestions ->
                    suggestionsState = SubjectSuggestionsUiState.Success(suggestions)
                }
                .onFailure { exception ->
                    suggestionsState = SubjectSuggestionsUiState.Error(
                        exception.message ?: "Failed to load suggestions"
                    )
                }
        }
    }

    fun selectPaperType(subjectCode: String, paperType: String) {
        loadSuggestions(subjectCode = subjectCode, paperType = paperType)
    }

    private suspend fun loadSubjectMetadata(subjectCode: String) {
        if (subject != null && subject?.subjectCode == subjectCode) {
            return
        }

        val result = subjectRepository.getSubjectOverview(subjectCode)

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

    private companion object {
        const val DEFAULT_QUERY = "important questions"
        const val DEFAULT_TOP_K = 10
    }
}
