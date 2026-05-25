package com.qarena.android.presentation.subjects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.core.analytics.AnalyticsTracker
import com.qarena.android.presentation.common.AnswerPayload
import com.qarena.android.presentation.subjects.components.NextStepActionCard
import com.qarena.android.presentation.subjects.components.QuestionCard
import com.qarena.android.presentation.subjects.components.QuestionsEmptyStateCard
import com.qarena.android.presentation.subjects.components.QuestionsErrorStateCard
import com.qarena.android.presentation.subjects.components.QuestionsLoadingState
import com.qarena.android.presentation.subjects.components.SubjectHeaderSection
import com.qarena.android.presentation.subjects.components.SubjectPublishedQuestionsHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailsScreen(
    subjectCode: String,
    onOpenQuestionsClick: (String) -> Unit,
    onOpenSuggestionsClick: (String) -> Unit,
    onOpenAnalysisClick: (String) -> Unit,
    onOpenPredictionsClick: (String) -> Unit,
    onGetAnswerClick: (AnswerPayload) -> Unit,
    subjectOverviewViewModel: SubjectOverviewViewModel = viewModel()
) {
    val overviewState = subjectOverviewViewModel.overviewState
    val questionsState = subjectOverviewViewModel.questionsState
    val subject = subjectOverviewViewModel.subject
    val selectedPaperType = subjectOverviewViewModel.selectedPaperType
    val supportedPaperTypes = subjectOverviewViewModel.supportedPaperTypes
    val totalCount = subjectOverviewViewModel.totalCount.takeIf { it > 0 }
        ?: subjectOverviewViewModel.questions.size

    LaunchedEffect(subjectCode) {
        AnalyticsTracker.trackScreen(
            screenName = "Subject Overview",
            path = "/android/subjects/$subjectCode/overview",
            subjectCode = subjectCode
        )
        subjectOverviewViewModel.loadSubjectData(subjectCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = subject?.subjectName ?: "Subject Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subject?.subjectCode ?: subjectCode,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = overviewState) {
                SubjectOverviewUiState.Idle,
                SubjectOverviewUiState.Loading -> {
                    item {
                        QuestionsLoadingState(label = "Gathering subject details...")
                    }
                }

                is SubjectOverviewUiState.Error -> {
                    item {
                        QuestionsErrorStateCard(
                            message = state.message,
                            onRetry = { subjectOverviewViewModel.loadSubjectData(subjectCode) }
                        )
                    }
                }

                is SubjectOverviewUiState.Success -> {
                    item {
                        SubjectHeaderSection(
                            subject = subject,
                            overview = state.overview,
                            fallbackSubjectCode = subjectCode
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Next Steps",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                NextStepActionCard(
                                    title = "Predictions",
                                    description = "See likely questions and high-value topics.",
                                    icon = Icons.Default.AutoAwesome,
                                    onClick = { onOpenPredictionsClick(subjectCode) },
                                    modifier = Modifier.weight(1f)
                                )
                                NextStepActionCard(
                                    title = "Analysis",
                                    description = "Review topic and year trends for this subject.",
                                    icon = Icons.Default.Insights,
                                    onClick = { onOpenAnalysisClick(subjectCode) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item {
                        SubjectPublishedQuestionsHeader(
                            totalCount = totalCount,
                            selectedPaperType = selectedPaperType,
                            supportedPaperTypes = supportedPaperTypes,
                            onPaperTypeSelected = { paperType ->
                                subjectOverviewViewModel.selectPaperType(subjectCode, paperType)
                            }
                        )
                    }

                    when (val questionState = questionsState) {
                        is QuestionsUiState.Loading -> {
                            item {
                                QuestionsLoadingState(label = "Loading questions...")
                            }
                        }

                        is QuestionsUiState.Error -> {
                            item {
                                QuestionsErrorStateCard(
                                    message = questionState.message,
                                    onRetry = { subjectOverviewViewModel.loadSubjectData(subjectCode, selectedPaperType) }
                                )
                            }
                        }

                        is QuestionsUiState.Success -> {
                            if (questionState.questions.isEmpty()) {
                                item {
                                    QuestionsEmptyStateCard()
                                }
                            } else {
                                itemsIndexed(questionState.questions) { index, question ->
                                    QuestionCard(
                                        question = question,
                                        subjectCode = subjectCode,
                                        questionIndex = index,
                                        onGetAnswerClick = onGetAnswerClick
                                    )
                                }
                            }
                        }

                        QuestionsUiState.Idle -> {
                            if (subjectOverviewViewModel.questions.isEmpty() && !subjectOverviewViewModel.isLoading) {
                                item {
                                    QuestionsEmptyStateCard()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
