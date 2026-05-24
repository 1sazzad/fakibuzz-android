package com.qarena.android.presentation.questions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.core.analytics.AnalyticsTracker
import com.qarena.android.model.displaySubtitle
import com.qarena.android.model.toSuggestion
import com.qarena.android.presentation.common.AnswerPayload
import com.qarena.android.util.QuestionPresentationLookups
import com.qarena.android.ui.components.*
import com.qarena.android.presentation.subjects.PaperTypeSelector

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            if (supportedPaperTypes.isNotEmpty()) {
                PaperTypeSelector(
                    supportedPaperTypes = supportedPaperTypes,
                    selectedPaperType = selectedPaperType,
                    onPaperTypeSelected = { paperType ->
                        questionsViewModel.selectPaperType(subjectCode, paperType)
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                questionsViewModel.isLoading -> {
                    QArenaLoadingState(label = "Loading questions...")
                }

                questionsViewModel.errorMessage != null -> {
                    QArenaErrorState(
                        message = questionsViewModel.errorMessage ?: "Failed to load questions",
                        onRetry = { questionsViewModel.loadQuestions(subjectCode) }
                    )
                }

                questionsViewModel.questions.isEmpty() -> {
                    QArenaEmptyState(
                        title = "No questions found",
                        description = "No questions match the selected paper type."
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(questionsViewModel.questions) { question ->
                            val suggestion = question.toSuggestion()
                            QuestionCard(
                                suggestion = suggestion,
                                onClick = {
                                    onGetAnswerClick(
                                        AnswerPayload(
                                            questionId = question.id,
                                            questionText = suggestion.questionText,
                                            prompt = QuestionPresentationLookups.buildAnswerPrompt(
                                                questionText = question.questionText,
                                                section = question.section,
                                                questionType = question.questionType,
                                                instruction = question.instruction,
                                                stem = question.stem,
                                                tableData = question.tableData,
                                                wordBox = question.wordBox,
                                                options = question.options,
                                                subQuestions = question.subQuestions,
                                                formulaLatex = question.formulaLatex,
                                                formulaDisplay = question.formulaDisplay,
                                                diagramDescription = question.diagramDescription,
                                                diagramReference = question.diagramReference,
                                                diagramType = question.diagramType,
                                                diagramSvg = question.diagramSvg,
                                                diagramRequired = question.diagramRequired,
                                                mathBlocks = question.mathBlocks
                                            ),
                                            subjectCode = subjectCode,
                                            academicLevel = question.academicLevel,
                                            paperType = question.paperType,
                                            topic = question.topic,
                                            marks = question.marks,
                                            formulaLatex = question.formulaLatex,
                                            formulaDisplay = question.formulaDisplay,
                                            diagramRequired = question.diagramRequired,
                                            diagramType = question.diagramType,
                                            diagramSvg = question.diagramSvg,
                                            diagramUrl = question.diagramUrl,
                                            diagramReference = question.diagramReference,
                                            diagramDescription = question.diagramDescription,
                                            mathBlocks = question.mathBlocks,
                                            answerType = question.answerType,
                                            section = question.section,
                                            instruction = question.instruction,
                                            stem = question.stem,
                                            questionType = question.questionType,
                                            tableData = question.tableData,
                                            wordBox = question.wordBox
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
