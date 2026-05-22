package com.qarena.android.presentation.subjects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.model.Subject
import com.qarena.android.data.repository.SubjectRepository
import kotlinx.coroutines.launch

sealed interface SubjectSearchUiState {
    data object Idle : SubjectSearchUiState
    data object Loading : SubjectSearchUiState
    data class Results(val subjects: List<Subject>) : SubjectSearchUiState
    data object Empty : SubjectSearchUiState
    data class Error(val message: String) : SubjectSearchUiState
}

class SubjectSearchViewModel : ViewModel() {

    private val subjectRepository = SubjectRepository()

    var searchQuery by mutableStateOf("")
        private set

    var searchState by mutableStateOf<SubjectSearchUiState>(SubjectSearchUiState.Idle)
        private set

    fun searchSubjects(query: String) {
        viewModelScope.launch {
            val trimmedQuery = query.trim()
            searchQuery = query

            if (trimmedQuery.isBlank()) {
                searchState = SubjectSearchUiState.Idle
                return@launch
            }

            searchState = SubjectSearchUiState.Loading

            val result = subjectRepository.searchSubjects(trimmedQuery)

            result
                .onSuccess { subjects ->
                    searchState = if (subjects.isEmpty()) {
                        SubjectSearchUiState.Empty
                    } else {
                        SubjectSearchUiState.Results(subjects)
                    }
                }
                .onFailure { exception ->
                    searchState = SubjectSearchUiState.Error(
                        exception.message ?: "Failed to search subjects"
                    )
                }
        }
    }

    fun clearSearch() {
        searchQuery = ""
        searchState = SubjectSearchUiState.Idle
    }
}
