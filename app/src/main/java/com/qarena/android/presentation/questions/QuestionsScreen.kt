package com.qarena.android.presentation.questions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.qarena.android.presentation.subjects.components.QuestionCard
import com.qarena.android.presentation.subjects.components.QuestionsEmptyStateCard
import com.qarena.android.presentation.subjects.components.QuestionsErrorStateCard
import com.qarena.android.presentation.subjects.components.QuestionsLoadingState
import com.qarena.android.presentation.subjects.components.SubjectPublishedQuestionsHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionsScreen(
    subjectCode: String,
    onGetAnswerClick: (AnswerPayload) -> Unit,
    questionsViewModel: QuestionsViewModel = viewModel()
) {
    LaunchedEffect(subjectCode) {
        AnalyticsTracker.trackScreen(
            screenName = "Questions",
            path = "/android/subjects/$subjectCode/questions",
            subjectCode = subjectCode
        )
        questionsViewModel.loadQuestions(subjectCode)
    }

    val subject = questionsViewModel.subject
    val supportedPaperTypes = questionsViewModel.supportedPaperTypes
    val selectedPaperType = questionsViewModel.selectedPaperType
    val totalCount = questionsViewModel.totalCount.takeIf { it > 0 } ?: questionsViewModel.questions.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = subject?.subjectName ?: "Questions",
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
            item {
                SubjectPublishedQuestionsHeader(
                    totalCount = totalCount,
                    selectedPaperType = selectedPaperType,
                    supportedPaperTypes = supportedPaperTypes,
                    onPaperTypeSelected = { paperType ->
                        questionsViewModel.selectPaperType(subjectCode, paperType)
                    }
                )
            }

            when {
                questionsViewModel.isLoading -> {
                    item {
                        QuestionsLoadingState(label = "Loading questions...")
                    }
                }

                questionsViewModel.errorMessage != null -> {
                    item {
                        QuestionsErrorStateCard(
                            message = questionsViewModel.errorMessage ?: "Failed to load questions",
                            onRetry = { questionsViewModel.loadQuestions(subjectCode, selectedPaperType) }
                        )
                    }
                }

                questionsViewModel.questions.isEmpty() -> {
                    item {
                        QuestionsEmptyStateCard(
                            title = "No questions found",
                            description = "No questions match the selected paper type."
                        )
                    }
                }

                else -> {
                    itemsIndexed(questionsViewModel.questions) { index, question ->
                        QuestionCard(
                            question = question,
                            subjectCode = subjectCode,
                            questionIndex = index,
                            onGetAnswerClick = onGetAnswerClick
                        )
                    }
                }
            }
        }
    }
}
