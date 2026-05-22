package com.qarena.android.presentation.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.core.analytics.AnalyticsTracker
import com.qarena.android.data.remote.dto.SearchResultResponse

@Composable
fun SearchScreen(
    onOpenSubjectOverviewClick: (String) -> Unit,
    onGetAnswerClick: (questionId: Int?, questionText: String, subjectCode: String, marks: Int?) -> Unit,
    searchViewModel: SearchViewModel = viewModel()
) {
    val searchState = searchViewModel.searchState

    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreen(
            screenName = "Search",
            path = "/android/search"
        )
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
                text = "Search",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = searchViewModel.query,
                onValueChange = { searchViewModel.onQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Search questions") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchViewModel.subjectCode,
                onValueChange = { searchViewModel.onSubjectCodeChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Subject code") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { searchViewModel.search() },
                modifier = Modifier.fillMaxWidth(),
                enabled = searchState !is SearchUiState.Loading
            ) {
                Text(
                    text = if (searchState is SearchUiState.Loading) {
                        "Searching..."
                    } else {
                        "Search"
                    }
                )
            }

            if (searchViewModel.query.isNotBlank() || searchViewModel.subjectCode.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { searchViewModel.clearSearch() }
                ) {
                    Text(text = "Clear")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SearchResultsContent(
                searchState = searchState,
                onOpenSubjectOverviewClick = onOpenSubjectOverviewClick,
                onGetAnswerClick = onGetAnswerClick
            )
        }
    }
}

@Composable
private fun SearchResultsContent(
    searchState: SearchUiState,
    onOpenSubjectOverviewClick: (String) -> Unit,
    onGetAnswerClick: (questionId: Int?, questionText: String, subjectCode: String, marks: Int?) -> Unit
) {
    when (searchState) {
        SearchUiState.Idle -> Unit
        SearchUiState.Loading -> {
            Text(text = "Searching...")
        }

        SearchUiState.Empty -> {
            Text(text = "No results found")
        }

        is SearchUiState.Error -> {
            Text(
                text = searchState.message,
                color = MaterialTheme.colorScheme.error
            )
        }

        is SearchUiState.Success -> {
            LazyColumn {
                items(searchState.results) { result ->
                    SearchResultCard(
                        result = result,
                        onOpenSubjectOverviewClick = onOpenSubjectOverviewClick,
                        onGetAnswerClick = onGetAnswerClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    result: SearchResultResponse,
    onOpenSubjectOverviewClick: (String) -> Unit,
    onGetAnswerClick: (questionId: Int?, questionText: String, subjectCode: String, marks: Int?) -> Unit
) {
    val subjectCode = result.subjectCode.orEmpty()
    val questionText = result.questionText.orEmpty()
    val score = result.similarityScore ?: result.score
    val year = result.examYear ?: result.year

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = questionText.ifBlank { "Question text unavailable" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            if (subjectCode.isNotBlank() || !result.subjectName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = listOfNotNull(
                        subjectCode.takeIf { it.isNotBlank() },
                        result.subjectName?.takeIf { it.isNotBlank() }
                    ).joinToString(" - "),
                    fontSize = 14.sp
                )
            }

            result.topic?.takeIf { it.isNotBlank() }?.let { topic ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Topic: $topic",
                    fontSize = 14.sp
                )
            }

            if (result.marks != null || year != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = listOfNotNull(
                        result.marks?.let { "Marks: $it" },
                        year?.let { "Year: $it" }
                    ).joinToString("   "),
                    fontSize = 14.sp
                )
            }

            score?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Score: $it",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onOpenSubjectOverviewClick(subjectCode) },
                enabled = subjectCode.isNotBlank()
            ) {
                Text(text = "Open Subject Overview")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onGetAnswerClick(
                        result.questionId,
                        questionText,
                        subjectCode,
                        result.marks
                    )
                },
                enabled = questionText.isNotBlank() && subjectCode.isNotBlank()
            ) {
                Text(text = "Get Answer")
            }
        }
    }
}
