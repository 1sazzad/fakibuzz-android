package com.qarena.android.presentation.subjects

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.core.analytics.AnalyticsTracker
import com.qarena.android.data.remote.dto.SubjectOverviewResponse
import com.qarena.android.model.displaySubtitle
import com.qarena.android.util.SubjectLookups

@Composable
fun SubjectOverviewScreen(
    subjectCode: String,
    onOpenQuestionsClick: (String) -> Unit,
    onOpenSuggestionsClick: (String) -> Unit,
    onOpenAnalysisClick: (String) -> Unit,
    onOpenPredictionsClick: (String) -> Unit,
    subjectOverviewViewModel: SubjectOverviewViewModel = viewModel()
) {
    val overviewState = subjectOverviewViewModel.overviewState

    LaunchedEffect(subjectCode) {
        AnalyticsTracker.trackScreen(
            screenName = "Subject Overview",
            path = "/android/subjects/$subjectCode/overview",
            subjectCode = subjectCode
        )
        subjectOverviewViewModel.loadSubjectOverview(subjectCode)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Subject Overview",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (overviewState) {
                SubjectOverviewUiState.Idle,
                SubjectOverviewUiState.Loading -> {
                    Text(text = "Loading subject overview...")
                }

                is SubjectOverviewUiState.Error -> {
                    Text(
                        text = overviewState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is SubjectOverviewUiState.Success -> {
                    SubjectOverviewContent(
                        overview = overviewState.overview,
                        fallbackSubjectCode = subjectCode,
                        onOpenQuestionsClick = onOpenQuestionsClick,
                        onOpenSuggestionsClick = onOpenSuggestionsClick,
                        onOpenAnalysisClick = onOpenAnalysisClick,
                        onOpenPredictionsClick = onOpenPredictionsClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectOverviewContent(
    overview: SubjectOverviewResponse,
    fallbackSubjectCode: String,
    onOpenQuestionsClick: (String) -> Unit,
    onOpenSuggestionsClick: (String) -> Unit,
    onOpenAnalysisClick: (String) -> Unit,
    onOpenPredictionsClick: (String) -> Unit
) {
    val subjectName = overview.subject?.subjectName
        ?: overview.subjectName
        ?: "Unnamed subject"
    val subjectCode = overview.subject?.subjectCode
        ?: overview.subjectCode
        ?: fallbackSubjectCode
    val totalQuestions = overview.totalQuestions
        ?: overview.questionCount

    if (subjectName == "Unnamed subject" && subjectCode.isBlank()) {
        Text(text = "No overview found")
        return
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = subjectName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subjectCode,
                fontSize = 16.sp
            )

            overview.subject?.let { subject ->
                SubjectLookups.run {
                    subject.toSubject().displaySubtitle()
                }?.let { subtitle ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            totalQuestions?.let { count ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Total questions: $count",
                    fontSize = 14.sp
                )
            }

            overview.summary?.takeIf { it.isNotBlank() }?.let { summary ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = summary)
            }
        }
    }

    OverviewListSection(
        title = "Years",
        values = overview.years
            ?.map { it.toString() }
            .orEmpty()
    )

    OverviewListSection(
        title = "Topics",
        values = overview.topics.orEmpty()
    )

    Spacer(modifier = Modifier.height(20.dp))

    Button(
        onClick = { onOpenQuestionsClick(subjectCode) },
        modifier = Modifier.fillMaxWidth(),
        enabled = subjectCode.isNotBlank()
    ) {
        Text(text = "Open Questions")
    }

    Spacer(modifier = Modifier.height(10.dp))

    Button(
        onClick = { onOpenSuggestionsClick(subjectCode) },
        modifier = Modifier.fillMaxWidth(),
        enabled = subjectCode.isNotBlank()
    ) {
        Text(text = "Open Suggestions")
    }

    Spacer(modifier = Modifier.height(10.dp))

    Button(
        onClick = { onOpenAnalysisClick(subjectCode) },
        modifier = Modifier.fillMaxWidth(),
        enabled = subjectCode.isNotBlank()
    ) {
        Text(text = "Open Analysis")
    }

    Spacer(modifier = Modifier.height(10.dp))

    Button(
        onClick = { onOpenPredictionsClick(subjectCode) },
        modifier = Modifier.fillMaxWidth(),
        enabled = subjectCode.isNotBlank()
    ) {
        Text(text = "Open Predictions")
    }
}

@Composable
private fun OverviewListSection(
    title: String,
    values: List<String>
) {
    if (values.isEmpty()) {
        return
    }

    Spacer(modifier = Modifier.height(16.dp))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            values.forEach { value ->
                Text(
                    text = value,
                    fontSize = 14.sp
                )
            }
        }
    }
}
