package com.qarena.android.presentation.subjects

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.core.analytics.AnalyticsTracker
import com.qarena.android.model.Suggestion
import com.qarena.android.model.displaySubtitle
import com.qarena.android.util.PaperTypeLookups
import com.qarena.android.util.QuestionPresentationLookups
import com.qarena.android.util.SuggestionLookups

@Composable
fun SubjectSuggestionsScreen(
    subjectCode: String,
    onGetAnswerClick: (questionId: Int?, questionText: String, marks: Int?) -> Unit,
    subjectSuggestionsViewModel: SubjectSuggestionsViewModel = viewModel()
) {
    val suggestionsState = subjectSuggestionsViewModel.suggestionsState
    val subject = subjectSuggestionsViewModel.subject
    val supportedPaperTypes = subjectSuggestionsViewModel.supportedPaperTypes
    val selectedPaperType = subjectSuggestionsViewModel.selectedPaperType

    LaunchedEffect(subjectCode) {
        AnalyticsTracker.trackScreen(
            screenName = "Suggestions",
            path = "/android/subjects/$subjectCode/suggestions",
            subjectCode = subjectCode
        )
        subjectSuggestionsViewModel.loadSuggestions(subjectCode)
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
                text = subject?.subjectName?.takeIf { it.isNotBlank() } ?: "Suggestions",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Subject: $subjectCode",
                fontSize = 16.sp
            )

            subject?.displaySubtitle()?.let { subtitle ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (supportedPaperTypes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                PaperTypeSelector(
                    supportedPaperTypes = supportedPaperTypes,
                    selectedPaperType = selectedPaperType,
                    onPaperTypeSelected = { paperType ->
                        subjectSuggestionsViewModel.selectPaperType(subjectCode, paperType)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = subjectSuggestionsViewModel.query,
                onValueChange = { subjectSuggestionsViewModel.onQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Suggestion query") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { subjectSuggestionsViewModel.loadSuggestions(subjectCode) },
                modifier = Modifier.fillMaxWidth(),
                enabled = suggestionsState !is SubjectSuggestionsUiState.Loading
            ) {
                Text(
                    text = if (suggestionsState is SubjectSuggestionsUiState.Loading) {
                        "Loading..."
                    } else {
                        "Load Suggestions"
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SuggestionsContent(
                suggestionsState = suggestionsState,
                onGetAnswerClick = onGetAnswerClick
            )
        }
    }
}

@Composable
private fun SuggestionsContent(
    suggestionsState: SubjectSuggestionsUiState,
    onGetAnswerClick: (questionId: Int?, questionText: String, marks: Int?) -> Unit
) {
    when (suggestionsState) {
        SubjectSuggestionsUiState.Idle -> Unit
        SubjectSuggestionsUiState.Loading -> {
            Text(text = "Loading suggestions...")
        }

        is SubjectSuggestionsUiState.Error -> {
            Text(
                text = suggestionsState.message,
                color = MaterialTheme.colorScheme.error
            )
        }

        is SubjectSuggestionsUiState.Success -> {
            if (suggestionsState.suggestions.isEmpty()) {
                Text(text = "No suggestions found")
            } else {
                LazyColumn {
                    items(suggestionsState.suggestions) { suggestion ->
                        SuggestionCard(
                            suggestion = suggestion,
                            onGetAnswerClick = onGetAnswerClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: Suggestion,
    onGetAnswerClick: (questionId: Int?, questionText: String, marks: Int?) -> Unit
) {
    val resolvedPaperType = PaperTypeLookups.normalizePaperType(suggestion.paperType)
    val questionText = QuestionPresentationLookups.displayQuestionText(suggestion.questionText, suggestion.stem)
    val hasQuestionContent = !suggestion.questionText.isBlank() ||
        !suggestion.section.isNullOrBlank() ||
        !suggestion.questionType.isNullOrBlank() ||
        !suggestion.instruction.isNullOrBlank() ||
        !suggestion.stem.isNullOrBlank() ||
        !suggestion.subQuestions.isNullOrEmpty() ||
        !suggestion.options.isNullOrEmpty() ||
        (suggestion.wordBox != null && !suggestion.wordBox.isJsonNull) ||
        (suggestion.tableData != null && !suggestion.tableData.isJsonNull) ||
        !suggestion.diagramDescription.isNullOrBlank() ||
        !suggestion.diagramReference.isNullOrBlank() ||
        (suggestion.mathBlocks != null && !suggestion.mathBlocks.isJsonNull)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            PaperTypeBadge(paperType = resolvedPaperType)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = suggestion.questionId?.let { "Question #$it" } ?: "Suggested question",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            SuggestionWrittenMetadata(
                section = suggestion.section,
                questionType = suggestion.questionType,
                instruction = suggestion.instruction
            )

            suggestion.stem?.trim()?.takeIf { it.isNotBlank() && it != questionText.trim() }?.let { stem ->
                Text(
                    text = stem,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = questionText.ifBlank { "No question text" },
                fontSize = 16.sp
            )

            suggestion.wordBox?.let { wordBox ->
                QuestionPresentationLookups.formatJsonContent(wordBox)?.let { text ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Word box: $text", fontSize = 14.sp)
                }
            }

            suggestion.subQuestions.orEmpty().takeIf { it.isNotEmpty() }?.let { subQuestions ->
                Spacer(modifier = Modifier.height(10.dp))
                subQuestions.forEachIndexed { index, subQuestion ->
                    val label = QuestionPresentationLookups.subQuestionLabel(index, subQuestion.label)
                    val subQuestionText = QuestionPresentationLookups.displayQuestionText(subQuestion.questionText)
                    Text(
                        text = "$label $subQuestionText",
                        fontSize = 14.sp
                    )

                    subQuestion.options.orEmpty().takeIf { it.isNotEmpty() }?.let { options ->
                        options.forEachIndexed { optionIndex, option ->
                            Text(
                                text = "${optionIndex + 1}. ${option.trim()}",
                                fontSize = 12.sp
                            )
                        }
                    }

                    subQuestion.diagramDescription?.trim()?.takeIf { it.isNotBlank() }?.let { description ->
                        Text(text = "Diagram: $description", fontSize = 12.sp)
                    }
                }
            }

            suggestion.options.orEmpty().takeIf { it.isNotEmpty() }?.let { options ->
                Spacer(modifier = Modifier.height(10.dp))
                options.forEachIndexed { index, option ->
                    Text(
                        text = "${index + 1}. ${option.trim()}",
                        fontSize = 14.sp
                    )
                }
            }

            suggestion.tableData?.let { tableData ->
                QuestionPresentationLookups.formatJsonContent(tableData)?.let { text ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Table data: $text", fontSize = 14.sp)
                }
            }

            suggestion.diagramDescription?.takeIf { it.isNotBlank() }?.let { description ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Diagram: $description", fontSize = 14.sp)
            }

            suggestion.diagramReference?.takeIf { it.isNotBlank() }?.let { reference ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Diagram reference: $reference", fontSize = 14.sp)
            }

            if (suggestion.diagramRequired == true) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Diagram/chart required", fontSize = 14.sp)
            }

            suggestion.mathBlocks?.takeIf { !it.isJsonNull }?.let { mathBlocks ->
                QuestionPresentationLookups.formatJsonContent(mathBlocks)?.let { text ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Math blocks: $text", fontSize = 14.sp)
                }
            }

            suggestion.topic?.takeIf { it.isNotBlank() }?.let { topic ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Topic: $topic",
                    fontSize = 14.sp
                )
            }

            (suggestion.examYear ?: suggestion.year)?.let { year ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Year: $year",
                    fontSize = 14.sp
                )
            }

            suggestion.marks?.let { marks ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Marks: $marks",
                    fontSize = 14.sp
                )
            }

            SuggestionLookups.formatPredictionScore(suggestion.predictionScore)?.let { score ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Prediction score: $score",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    onGetAnswerClick(
                        suggestion.questionId,
                        QuestionPresentationLookups.buildAnswerPrompt(
                            questionText = suggestion.questionText,
                            section = suggestion.section,
                            questionType = suggestion.questionType,
                            instruction = suggestion.instruction,
                            stem = suggestion.stem,
                            tableData = suggestion.tableData,
                            wordBox = suggestion.wordBox,
                            options = suggestion.options,
                            subQuestions = suggestion.subQuestions,
                            diagramDescription = suggestion.diagramDescription,
                            diagramReference = suggestion.diagramReference,
                            diagramRequired = suggestion.diagramRequired,
                            mathBlocks = suggestion.mathBlocks
                        ),
                        suggestion.marks
                    )
                },
                enabled = hasQuestionContent
            ) {
                Text(text = "Get Answer")
            }
        }
    }
}

@Composable
private fun SuggestionWrittenMetadata(
    section: String?,
    questionType: String?,
    instruction: String?
) {
    section?.trim()?.takeIf { it.isNotBlank() }?.let { text ->
        Text(text = "Section: $text", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
    }

    questionType?.trim()?.takeIf { it.isNotBlank() }?.let { text ->
        Text(text = "Question type: $text", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
    }

    instruction?.trim()?.takeIf { it.isNotBlank() }?.let { text ->
        Text(text = "Instruction: $text", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
    }
}
