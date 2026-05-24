package com.qarena.android.presentation.answers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.core.analytics.AnalyticsTracker
import com.qarena.android.presentation.common.AnswerPayload
import com.qarena.android.presentation.common.DiagramInfo
import com.qarena.android.presentation.common.DiagramRenderer
import com.qarena.android.ui.components.QArenaErrorState
import com.qarena.android.ui.components.QArenaLoadingState
import com.qarena.android.ui.components.QArenaPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerScreen(
    subjectCode: String,
    answerPayload: AnswerPayload,
    answerViewModel: AnswerViewModel = viewModel()
) {
    val answerState = answerViewModel.answerState
    val resolvedSubjectCode = answerPayload.subjectCode.ifBlank { subjectCode }
    val questionText = answerPayload.questionText.ifBlank {
        answerPayload.prompt ?: "No question text selected"
    }
    val diagramInfo = DiagramInfo(
        diagramRequired = answerPayload.diagramRequired,
        diagramType = answerPayload.diagramType,
        diagramSvg = answerPayload.diagramSvg,
        diagramUrl = answerPayload.diagramUrl,
        diagramDescription = answerPayload.diagramDescription,
        diagramReference = answerPayload.diagramReference
    )

    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreen(
            screenName = "Answer",
            path = "/android/answer",
            subjectCode = resolvedSubjectCode,
            questionId = answerPayload.questionId
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Solution", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Question Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Question",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = questionText.ifBlank { "No question text selected" },
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 26.sp
                    )

                    DiagramRenderer(diagramInfo = diagramInfo)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(text = resolvedSubjectCode)
                        answerPayload.marks?.let { SuggestionChip(text = "$it Marks") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            if (answerState is AnswerUiState.Idle) {
                QArenaPrimaryButton(
                    text = "Generate AI Answer",
                    onClick = {
                        answerViewModel.generateAnswer(answerPayload.copy(subjectCode = resolvedSubjectCode).toGenerateAnswerRequest())
                    }
                )
            }

            // Results Section
            when (answerState) {
                AnswerUiState.Idle -> Unit
                AnswerUiState.Loading -> {
                    QArenaLoadingState(label = "Thinking...")
                }

                is AnswerUiState.Polling -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = answerState.message ?: "Generating your answer...",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                is AnswerUiState.Error -> {
                    QArenaErrorState(
                        message = answerState.message,
                        onRetry = {
                            answerViewModel.generateAnswer(answerPayload.copy(subjectCode = resolvedSubjectCode).toGenerateAnswerRequest())
                        }
                    )
                }

                is AnswerUiState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Generated Answer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Actual Answer Text
                            Text(
                                text = answerState.answerText,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 28.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Answers are AI-generated. Please verify with your textbook.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SuggestionChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
