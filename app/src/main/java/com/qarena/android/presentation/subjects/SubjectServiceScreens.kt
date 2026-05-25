package com.qarena.android.presentation.subjects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.core.analytics.AnalyticsTracker
import com.qarena.android.presentation.answers.AnswerViewModel
import com.qarena.android.presentation.common.AnswerPayload
import com.qarena.android.presentation.common.DiagramRenderer
import com.qarena.android.presentation.common.toDiagramInfo
import com.qarena.android.presentation.navigation.SelectedSubjectNavArgs
import com.qarena.android.ui.components.QArenaErrorState
import com.qarena.android.ui.components.QArenaLoadingState
import com.qarena.android.ui.components.QArenaPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimilarQuestionsScreen(
    selectedSubjectNavArgs: SelectedSubjectNavArgs,
    onGetAnswerClick: (AnswerPayload) -> Unit,
    subjectSuggestionsViewModel: SubjectSuggestionsViewModel = viewModel()
) {
    SubjectSuggestionsScreen(
        selectedSubjectNavArgs = selectedSubjectNavArgs,
        onGetAnswerClick = onGetAnswerClick,
        subjectSuggestionsViewModel = subjectSuggestionsViewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicAnalysisScreen(
    selectedSubjectNavArgs: SelectedSubjectNavArgs,
    subjectAnalysisViewModel: SubjectAnalysisViewModel = viewModel()
) {
    SubjectAnalysisScreen(
        subjectCode = selectedSubjectNavArgs.subjectCode,
        subjectName = selectedSubjectNavArgs.subjectName,
        screenTitle = "Topic Analysis",
        paperType = selectedSubjectNavArgs.paperType,
        subjectAnalysisViewModel = subjectAnalysisViewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerBuilderScreen(
    selectedSubjectNavArgs: SelectedSubjectNavArgs,
    answerViewModel: AnswerViewModel = viewModel()
) {
    var prompt by remember { mutableStateOf("") }
    val answerState = answerViewModel.answerState

    LaunchedEffect(selectedSubjectNavArgs.subjectCode) {
        AnalyticsTracker.trackScreen(
            screenName = "Answer Help",
            path = "/android/subjects/${selectedSubjectNavArgs.subjectCode}/answer-builder",
            subjectCode = selectedSubjectNavArgs.subjectCode
        )
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Answer Help",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedSubjectNavArgs.subjectName.orEmpty().ifBlank { selectedSubjectNavArgs.subjectCode },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SubjectMiniInfoCard(
                title = selectedSubjectNavArgs.subjectName.orEmpty().ifBlank { "Subject" },
                subjectCode = selectedSubjectNavArgs.subjectCode,
                subtitle = "Draft an exam-style answer."
            )

            androidx.compose.material3.OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Prompt") },
                placeholder = { Text(text = "Describe the answer you want to draft") },
                minLines = 4
            )

            QArenaPrimaryButton(
                text = when (answerState) {
                    is com.qarena.android.presentation.answers.AnswerUiState.Loading -> "Generating..."
                    is com.qarena.android.presentation.answers.AnswerUiState.Polling -> "Generating..."
                    else -> "Generate Answer"
                },
                onClick = {
                    answerViewModel.generateAnswer(
                        AnswerPayload(
                            subjectCode = selectedSubjectNavArgs.subjectCode,
                            questionText = prompt,
                            prompt = prompt
                        ).toGenerateAnswerRequest()
                    )
                },
                enabled = prompt.isNotBlank() && answerState !is com.qarena.android.presentation.answers.AnswerUiState.Loading
            )

            when (answerState) {
                com.qarena.android.presentation.answers.AnswerUiState.Idle -> {
                    Text(
                        text = "Write a prompt and generate a draft answer.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                com.qarena.android.presentation.answers.AnswerUiState.Loading -> {
                    QArenaLoadingState(label = "Generating answer...")
                }

                is com.qarena.android.presentation.answers.AnswerUiState.Polling -> {
                    QArenaLoadingState(label = answerState.message ?: "Checking job status...")
                }

                is com.qarena.android.presentation.answers.AnswerUiState.Error -> {
                    QArenaErrorState(
                        message = answerState.message,
                        onRetry = {
                            answerViewModel.generateAnswer(
                                AnswerPayload(
                                    subjectCode = selectedSubjectNavArgs.subjectCode,
                                    questionText = prompt,
                                    prompt = prompt
                                ).toGenerateAnswerRequest()
                            )
                        }
                    )
                }

                is com.qarena.android.presentation.answers.AnswerUiState.Success -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Draft answer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = answerState.answerText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectMiniInfoCard(
    title: String,
    subjectCode: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subjectCode,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
