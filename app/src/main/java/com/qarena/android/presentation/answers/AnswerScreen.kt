package com.qarena.android.presentation.answers

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
import com.qarena.android.data.remote.dto.GenerateAnswerRequest

@Composable
fun AnswerScreen(
    subjectCode: String,
    questionId: Int?,
    questionText: String,
    marks: Int?,
    answerViewModel: AnswerViewModel = viewModel()
) {
    val answerState = answerViewModel.answerState

    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreen(
            screenName = "Answer",
            path = "/android/answer",
            subjectCode = subjectCode,
            questionId = questionId
        )
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
                text = "Answer",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Subject: $subjectCode",
                fontSize = 16.sp
            )

            marks?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Marks: $it",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Question",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = questionText.ifBlank { "No question text selected" },
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    answerViewModel.generateAnswer(
                        GenerateAnswerRequest(
                            questionId = questionId,
                            questionText = questionText,
                            subjectCode = subjectCode
                        )
                    )
                },
                enabled = questionText.isNotBlank() &&
                    answerState !is AnswerUiState.Loading &&
                    answerState !is AnswerUiState.Polling
            ) {
                Text(
                    text = if (
                        answerState is AnswerUiState.Loading ||
                        answerState is AnswerUiState.Polling
                    ) {
                        "Generating..."
                    } else {
                        "Generate Answer"
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (answerState) {
                AnswerUiState.Idle -> Unit
                AnswerUiState.Loading -> {
                    Text(text = "Generating answer...")
                }

                is AnswerUiState.Polling -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Generating answer...",
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            answerState.status?.let { status ->
                                Text(text = "Status: $status")
                            }

                            answerState.progress?.let { progress ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Progress: $progress")
                            }

                            answerState.message?.let { message ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = message)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Job ID: ${answerState.jobId}",
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                is AnswerUiState.Error -> {
                    Text(
                        text = answerState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is AnswerUiState.Success -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Generated Answer",
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = answerState.answerText)

                            answerState.status?.let { status ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Status: $status",
                                    fontSize = 12.sp
                                )
                            }

                            answerState.jobId?.let { jobId ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Job ID: $jobId",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
