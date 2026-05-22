package com.qarena.android.presentation.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.data.remote.dto.SearchRequest
import com.qarena.android.data.remote.dto.SearchResultResponse
import com.qarena.android.data.repository.SearchRepository
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val results: List<SearchResultResponse>) : SearchUiState
    data object Empty : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel : ViewModel() {

    private val searchRepository = SearchRepository()

    var query by mutableStateOf("")
        private set

    var subjectCode by mutableStateOf("")
        private set

    var searchState by mutableStateOf<SearchUiState>(SearchUiState.Idle)
        private set

    fun onQueryChange(newQuery: String) {
        query = newQuery
        searchState = SearchUiState.Idle
    }

    fun onSubjectCodeChange(newSubjectCode: String) {
        subjectCode = newSubjectCode
        searchState = SearchUiState.Idle
    }

    fun search(
        query: String = this.query,
        subjectCode: String? = this.subjectCode,
        topK: Int = DEFAULT_TOP_K
    ) {
        viewModelScope.launch {
            val trimmedQuery = query.trim()
            val trimmedSubjectCode = subjectCode?.trim()?.takeIf { it.isNotBlank() }

            if (trimmedQuery.isBlank()) {
                searchState = SearchUiState.Error("Search query is required")
                return@launch
            }

            this@SearchViewModel.query = query
            this@SearchViewModel.subjectCode = trimmedSubjectCode.orEmpty()
            searchState = SearchUiState.Loading

            val result = searchRepository.searchQuestions(
                SearchRequest(
                    query = trimmedQuery,
                    subjectCode = trimmedSubjectCode,
                    topK = topK
                )
            )

            result
                .onSuccess { response ->
                    val results = response.results ?: response.questions.orEmpty()
                    searchState = if (results.isEmpty()) {
                        SearchUiState.Empty
                    } else {
                        SearchUiState.Success(results)
                    }
                }
                .onFailure { exception ->
                    searchState = SearchUiState.Error(
                        exception.message ?: "Search failed"
                    )
                }
        }
    }

    fun clearSearch() {
        query = ""
        subjectCode = ""
        searchState = SearchUiState.Idle
    }

    private companion object {
        const val DEFAULT_TOP_K = 10
    }
}
