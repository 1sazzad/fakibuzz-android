package com.qarena.android.presentation.subjects

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
import com.qarena.android.core.session.SessionManager
import com.qarena.android.model.Suggestion
import com.qarena.android.model.displayMarks
import com.qarena.android.model.displayLabel
import com.qarena.android.model.displayPaperType
import com.qarena.android.model.displayQuestionText
import com.qarena.android.model.displayScore
import com.qarena.android.model.displayTopic
import com.qarena.android.model.displaySubtitle
import com.qarena.android.presentation.common.AnswerPayload
import com.qarena.android.presentation.common.DiagramRenderer
import com.qarena.android.presentation.common.toDiagramInfo
import com.qarena.android.presentation.navigation.SelectedSubjectNavArgs
import com.qarena.android.ui.components.*
import com.qarena.android.util.AcademicProfile
import com.qarena.android.util.PaperTypeLookups
import com.qarena.android.util.QuestionPresentationLookups

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectSuggestionsScreen(
    selectedSubjectNavArgs: SelectedSubjectNavArgs,
    onGetAnswerClick: (AnswerPayload) -> Unit,
    subjectSuggestionsViewModel: SubjectSuggestionsViewModel = viewModel()
) {
    val suggestionsState = subjectSuggestionsViewModel.suggestionsState
    val subject = subjectSuggestionsViewModel.subject
    val supportedPaperTypes = subjectSuggestionsViewModel.supportedPaperTypes
    val selectedPaperType = subjectSuggestionsViewModel.selectedPaperType
    val shouldShowPaperTypeSelector =
        supportedPaperTypes.isNotEmpty() &&
            AcademicProfile.isSyllabusScoped(subject?.academicLevel ?: SessionManager.userAcademicLevel)

    LaunchedEffect(selectedSubjectNavArgs.subjectCode, selectedSubjectNavArgs.paperType) {
        AnalyticsTracker.trackScreen(
            screenName = "Suggestions",
            path = "/android/subjects/${selectedSubjectNavArgs.subjectCode}/suggestions",
            subjectCode = selectedSubjectNavArgs.subjectCode
        )
        subjectSuggestionsViewModel.loadSuggestions(
            selectedSubjectNavArgs.subjectCode,
            paperType = selectedSubjectNavArgs.paperType
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = selectedSubjectNavArgs.subjectName ?: subject?.subjectName ?: "Suggestions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subject?.subjectCode ?: selectedSubjectNavArgs.subjectCode,
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
            if (shouldShowPaperTypeSelector) {
                PaperTypeSelector(
                    supportedPaperTypes = supportedPaperTypes,
                    selectedPaperType = selectedPaperType,
                    onPaperTypeSelected = { paperType ->
                        subjectSuggestionsViewModel.selectPaperType(selectedSubjectNavArgs.subjectCode, paperType)
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            OutlinedTextField(
                value = subjectSuggestionsViewModel.query,
                onValueChange = { subjectSuggestionsViewModel.onQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Search for topics or questions") },
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            QArenaPrimaryButton(
                text = if (suggestionsState is SubjectSuggestionsUiState.Loading) {
                    "Generating Suggestions..."
                } else {
                    "Get Suggestions"
                },
                onClick = { subjectSuggestionsViewModel.loadSuggestions(selectedSubjectNavArgs.subjectCode) },
                enabled = suggestionsState !is SubjectSuggestionsUiState.Loading
            )

            Spacer(modifier = Modifier.height(20.dp))

            SuggestionsContent(
                suggestionsState = suggestionsState,
                subjectCode = selectedSubjectNavArgs.subjectCode,
                subjectAcademicLevel = subject?.academicLevel,
                onGetAnswerClick = onGetAnswerClick,
                onRetry = { subjectSuggestionsViewModel.loadSuggestions(selectedSubjectNavArgs.subjectCode) }
            )
        }
    }
}

@Composable
private fun SuggestionsContent(
    suggestionsState: SubjectSuggestionsUiState,
    subjectCode: String,
    subjectAcademicLevel: String?,
    onGetAnswerClick: (AnswerPayload) -> Unit,
    onRetry: () -> Unit
) {
    when (suggestionsState) {
        SubjectSuggestionsUiState.Idle -> Unit
        SubjectSuggestionsUiState.Loading -> {
            QArenaLoadingState(label = "Analyzing previous years...")
        }

        is SubjectSuggestionsUiState.Error -> {
            QArenaErrorState(
                message = suggestionsState.message,
                onRetry = onRetry
            )
        }

        is SubjectSuggestionsUiState.Success -> {
            if (suggestionsState.suggestions.isEmpty()) {
                QArenaEmptyState(
                    title = "No suggestions found",
                    description = "Try adjusting your search query or choosing a different paper type."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(suggestionsState.suggestions) { suggestion ->
                        QuestionCard(
                            suggestion = suggestion,
                            onClick = {
                                val questionText = suggestion.displayQuestionText()
                                val resolvedPaperType = PaperTypeLookups.normalizePaperType(suggestion.displayPaperType())
                                onGetAnswerClick(
                                    AnswerPayload(
                                        questionId = suggestion.questionId,
                                        questionText = questionText,
                                        prompt = QuestionPresentationLookups.buildAnswerPrompt(
                                            questionText = suggestion.questionText,
                                            section = suggestion.section,
                                            questionType = suggestion.questionType,
                                            instruction = suggestion.instruction,
                                            stem = suggestion.stem,
                                            tableData = suggestion.tableData,
                                            wordBox = suggestion.wordBox,
                                            options = suggestion.options,
                                            subQuestions = suggestion.subQuestions,
                                            formulaLatex = suggestion.formulaLatex,
                                            formulaDisplay = suggestion.formulaDisplay,
                                            diagramDescription = suggestion.diagramDescription,
                                            diagramReference = suggestion.diagramReference,
                                            diagramType = suggestion.diagramType,
                                            diagramSvg = suggestion.diagramSvg,
                                            diagramRequired = suggestion.diagramRequired,
                                            mathBlocks = suggestion.mathBlocks
                                        ),
                                        subjectCode = subjectCode,
                                        academicLevel = subjectAcademicLevel,
                                        paperType = resolvedPaperType,
                                        topic = suggestion.topic,
                                        marks = suggestion.marks,
                                        formulaLatex = suggestion.formulaLatex,
                                        formulaDisplay = suggestion.formulaDisplay,
                                        diagramRequired = suggestion.diagramRequired,
                                        diagramType = suggestion.diagramType,
                                        diagramSvg = suggestion.diagramSvg,
                                        diagramUrl = suggestion.diagramUrl,
                                        diagramReference = suggestion.diagramReference,
                                        diagramDescription = suggestion.diagramDescription,
                                        mathBlocks = suggestion.mathBlocks,
                                        answerType = suggestion.questionType,
                                        section = suggestion.section,
                                        instruction = suggestion.instruction,
                                        stem = suggestion.stem,
                                        questionType = suggestion.questionType,
                                        tableData = suggestion.tableData,
                                        wordBox = suggestion.wordBox
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
