package com.qarena.android.presentation.subjects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.model.Subject
import com.qarena.android.model.displaySubtitle
import com.qarena.android.util.SubjectLookups
import kotlinx.coroutines.delay

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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Subjects",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Search subjects")
                },
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        TextButton(
                            onClick = {
                                searchQuery = ""
                                subjectSearchViewModel.clearSearch()
                            }
                        ) {
                            Text(text = "Clear")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                    onSubjectClick = onSubjectClick
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
            Text(text = "Searching subjects...")
        }

        SubjectSearchUiState.Empty -> {
            Text(text = "No matching subjects found")
        }

        is SubjectSearchUiState.Error -> {
            Text(
                text = searchState.message,
                color = MaterialTheme.colorScheme.error
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
    onSubjectClick: (String) -> Unit
) {
    if (isLoading) {
        Text(text = "Loading subjects...")
    } else if (errorMessage != null) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error
        )
    } else if (subjects.isEmpty()) {
        Text(text = "No subjects found")
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
    LazyColumn {
        items(subjects) { subject ->
            SubjectCard(
                subject = subject,
                onSubjectClick = onSubjectClick
            )
        }
    }
}

@Composable
private fun SubjectCard(
    subject: Subject,
    onSubjectClick: (String) -> Unit
) {
    val subjectCode = subject.subjectCode

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !subjectCode.isNullOrBlank()
            ) {
                if (!subjectCode.isNullOrBlank()) {
                    onSubjectClick(subjectCode)
                }
            }
            .padding(bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = subject.subjectName.ifBlank { "Unnamed subject" },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subject.subjectCode.ifBlank { "No code" },
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            subject.displaySubtitle()?.let { subtitle ->
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = SubjectLookups.formatLabel(subject),
                fontSize = 14.sp
            )
        }
    }
}

private const val SEARCH_DEBOUNCE_MILLIS = 350L
