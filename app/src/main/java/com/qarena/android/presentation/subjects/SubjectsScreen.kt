package com.qarena.android.presentation.subjects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.model.Subject
import com.qarena.android.ui.components.QArenaEmptyState
import com.qarena.android.ui.components.QArenaErrorState
import com.qarena.android.ui.components.QArenaLoadingState
import com.qarena.android.ui.components.SubjectCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    onSubjectClick: (String) -> Unit,
    subjectsViewModel: SubjectsViewModel = viewModel(),
    subjectSearchViewModel: SubjectSearchViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        subjectsViewModel.loadSubjects()
    }

    var searchQuery by remember { mutableStateOf("") }
    val searchState = subjectSearchViewModel.searchState
    val isSearching = searchQuery.trim().isNotBlank()

    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().isBlank()) {
            subjectSearchViewModel.clearSearch()
            return@LaunchedEffect
        }

        delay(SEARCH_DEBOUNCE_MILLIS)
        subjectSearchViewModel.searchSubjects(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Subjects",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Search subjects by name or code...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                subjectSearchViewModel.clearSearch()
                            }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isSearching) {
                SearchResultsContent(
                    searchState = searchState,
                    onSubjectClick = onSubjectClick
                )
            } else {
                SubjectListContent(
                    isLoading = subjectsViewModel.isLoading,
                    errorMessage = subjectsViewModel.errorMessage,
                    subjects = subjectsViewModel.subjects,
                    onSubjectClick = onSubjectClick,
                    onRetry = { subjectsViewModel.loadSubjects() }
                )
            }
        }
    }
}

@Composable
private fun SearchResultsContent(
    searchState: SubjectSearchUiState,
    onSubjectClick: (String) -> Unit
) {
    when (searchState) {
        SubjectSearchUiState.Idle,
        SubjectSearchUiState.Loading -> {
            QArenaLoadingState(label = "Searching...")
        }

        SubjectSearchUiState.Empty -> {
            QArenaEmptyState(
                title = "No subjects found",
                description = "We couldn't find any subjects matching your search."
            )
        }

        is SubjectSearchUiState.Error -> {
            QArenaErrorState(
                message = searchState.message,
                onRetry = { /* Search retry handled by LaunchedEffect on query change */ }
            )
        }

        is SubjectSearchUiState.Results -> {
            SubjectList(
                subjects = searchState.subjects,
                onSubjectClick = onSubjectClick
            )
        }
    }
}

@Composable
private fun SubjectListContent(
    isLoading: Boolean,
    errorMessage: String?,
    subjects: List<Subject>,
    onSubjectClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    if (isLoading) {
        QArenaLoadingState(label = "Loading your curriculum...")
    } else if (errorMessage != null) {
        QArenaErrorState(
            message = errorMessage,
            onRetry = onRetry
        )
    } else if (subjects.isEmpty()) {
        QArenaEmptyState(
            title = "Your library is empty",
            description = "Complete your academic profile to see available subjects."
        )
    } else {
        SubjectList(
            subjects = subjects,
            onSubjectClick = onSubjectClick
        )
    }
}

@Composable
private fun SubjectList(
    subjects: List<Subject>,
    onSubjectClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(subjects) { subject ->
            SubjectCard(
                subject = subject,
                onClick = {
                    if (subject.subjectCode.isNotBlank()) {
                        onSubjectClick(subject.subjectCode)
                    }
                }
            )
        }
    }
}

private const val SEARCH_DEBOUNCE_MILLIS = 350L
