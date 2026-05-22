package com.qarena.android.presentation.questions

import androidx.compose.foundation.layout.Arrangement
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
import com.qarena.android.data.remote.dto.QuestionResponse
import com.qarena.android.model.SubQuestion
import com.qarena.android.model.displaySubtitle
import com.qarena.android.presentation.subjects.PaperTypeBadge
import com.qarena.android.presentation.subjects.PaperTypeSelector
import com.qarena.android.util.PaperTypeLookups
import com.qarena.android.util.QuestionPresentationLookups

@Composable
fun QuestionsScreen(
    subjectCode: String,
    onGetAnswerClick: (questionId: Int?, questionText: String, marks: Int?) -> Unit,
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
                text = subject?.subjectName?.takeIf { it.isNotBlank() } ?: "Questions",
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
                        questionsViewModel.selectPaperType(subjectCode, paperType)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            when {
                questionsViewModel.isLoading -> {
                    Text(
                        text = when (PaperTypeLookups.normalizePaperType(selectedPaperType)) {
                            PaperTypeLookups.CQ -> "Loading CQ questions..."
                            PaperTypeLookups.MCQ -> "Loading MCQ questions..."
                            PaperTypeLookups.WRITTEN -> "Loading written questions..."
                            else -> "Loading questions..."
                        }
                    )
                }

                questionsViewModel.errorMessage != null -> {
                    Text(
                        text = questionsViewModel.errorMessage ?: "Failed to load questions",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                questionsViewModel.questions.isEmpty() -> {
                    Text(
                        text = when (PaperTypeLookups.normalizePaperType(selectedPaperType)) {
                            PaperTypeLookups.CQ -> "No CQ questions found for this subject."
                            PaperTypeLookups.MCQ -> "No MCQ questions found for this subject."
                            PaperTypeLookups.WRITTEN -> "No written questions found for this subject."
                            else -> "No questions found"
                        }
                    )
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(questionsViewModel.questions) { question ->
                            QuestionCard(
                                question = question,
                                selectedPaperType = selectedPaperType,
                                onGetAnswerClick = onGetAnswerClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(
    question: QuestionResponse,
    selectedPaperType: String?,
    onGetAnswerClick: (questionId: Int?, questionText: String, marks: Int?) -> Unit
) {
    val resolvedPaperType = PaperTypeLookups.normalizePaperType(question.paperType) ?: PaperTypeLookups.normalizePaperType(selectedPaperType)
    val isMcq = resolvedPaperType == PaperTypeLookups.MCQ
    val isCq = resolvedPaperType == PaperTypeLookups.CQ
    val isWritten = resolvedPaperType == PaperTypeLookups.WRITTEN
    val hasQuestionText = !question.questionText.isNullOrBlank() ||
        !question.section.isNullOrBlank() ||
        !question.questionType.isNullOrBlank() ||
        !question.instruction.isNullOrBlank() ||
        !question.stem.isNullOrBlank() ||
        !question.subQuestions.isNullOrEmpty() ||
        !question.options.isNullOrEmpty() ||
        (question.wordBox != null && !question.wordBox.isJsonNull) ||
        (question.tableData != null && !question.tableData.isJsonNull) ||
        !question.formulaLatex.isNullOrBlank() ||
        !question.formulaDisplay.isNullOrBlank() ||
        !question.diagramDescription.isNullOrBlank() ||
        !question.diagramReference.isNullOrBlank() ||
        (question.mathBlocks != null && !question.mathBlocks.isJsonNull)

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            PaperTypeBadge(paperType = resolvedPaperType)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = question.questionNo ?: "Q",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            WrittenMetadata(
                section = question.section,
                questionType = question.questionType,
                instruction = question.instruction
            )

            when {
                isMcq -> {
                    Text(
                        text = QuestionPresentationLookups.displayQuestionText(question.questionText, question.stem),
                        fontSize = 16.sp
                    )

                    question.options.orEmpty().takeIf { it.isNotEmpty() }?.let { options ->
                        Spacer(modifier = Modifier.height(10.dp))
                        options.forEachIndexed { index, option ->
                            Text(
                                text = "${index + 1}. ${option.trim()}",
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                isCq || isWritten -> {
                    question.stem?.trim()?.takeIf { it.isNotBlank() }?.let { stem ->
                        Text(
                            text = stem,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    question.questionText?.trim()?.takeIf { it.isNotBlank() && it != question.stem?.trim() }?.let { text ->
                        Text(
                            text = text,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    question.wordBox?.let { wordBox ->
                        QuestionPresentationLookups.formatJsonContent(wordBox)?.let { text ->
                            Text(text = "Word box: $text", fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    question.subQuestions.orEmpty().takeIf { it.isNotEmpty() }?.let { subQuestions ->
                        subQuestions.forEachIndexed { index, subQuestion ->
                            SubQuestionItem(
                                index = index,
                                subQuestion = subQuestion
                            )
                        }
                    }

                    question.options.orEmpty().takeIf { it.isNotEmpty() }?.let { options ->
                        Spacer(modifier = Modifier.height(10.dp))
                        options.forEachIndexed { index, option ->
                            Text(
                                text = "${index + 1}. ${option.trim()}",
                                fontSize = 14.sp
                            )
                        }
                    }

                    question.tableData?.let { tableData ->
                        QuestionPresentationLookups.formatJsonContent(tableData)?.let { text ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Table data: $text", fontSize = 14.sp)
                        }
                    }

                    question.formulaDisplay?.trim()?.takeIf { it.isNotBlank() }?.let { formula ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Formula: $formula", fontSize = 14.sp)
                    }

                    question.formulaLatex?.trim()?.takeIf { it.isNotBlank() }?.let { formula ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Formula (LaTeX): $formula", fontSize = 14.sp)
                    }

                    question.diagramDescription?.trim()?.takeIf { it.isNotBlank() }?.let { description ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Diagram: $description", fontSize = 14.sp)
                    }

                    question.diagramReference?.trim()?.takeIf { it.isNotBlank() }?.let { reference ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Diagram reference: $reference", fontSize = 14.sp)
                    }

                    if (question.diagramRequired == true) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Diagram/chart required", fontSize = 14.sp)
                    }

                    question.mathBlocks?.takeIf { !it.isJsonNull }?.let { mathBlocks ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Math blocks: ${QuestionPresentationLookups.formatJsonContent(mathBlocks)}",
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    Text(
                        text = QuestionPresentationLookups.displayQuestionText(question.questionText, question.stem),
                        fontSize = 16.sp
                    )

                    question.options.orEmpty().takeIf { it.isNotEmpty() }?.let { options ->
                        Spacer(modifier = Modifier.height(10.dp))
                        options.forEachIndexed { index, option ->
                            Text(
                                text = "${index + 1}. ${option.trim()}",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            question.marks?.let { marks ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Marks: $marks",
                    fontSize = 14.sp
                )
            }

            question.topic?.takeIf { it.isNotBlank() }?.let { topic ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Topic: $topic",
                    fontSize = 14.sp
                )
            }

            question.examYear?.let { examYear ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Exam year: $examYear",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    onGetAnswerClick(
                        question.id,
                        QuestionPresentationLookups.buildAnswerPrompt(
                            questionText = question.questionText,
                            section = question.section,
                            questionType = question.questionType,
                            instruction = question.instruction,
                            stem = question.stem,
                            tableData = question.tableData,
                            wordBox = question.wordBox,
                            options = if (isMcq || isWritten) question.options else null,
                            subQuestions = question.subQuestions,
                            formulaLatex = question.formulaLatex,
                            formulaDisplay = question.formulaDisplay,
                            diagramDescription = question.diagramDescription,
                            diagramReference = question.diagramReference,
                            diagramRequired = question.diagramRequired,
                            mathBlocks = question.mathBlocks
                        ),
                        question.marks
                    )
                },
                enabled = hasQuestionText
            ) {
                Text(text = "Get Answer")
            }
        }
    }
}

@Composable
private fun WrittenMetadata(
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

@Composable
private fun SubQuestionItem(
    index: Int,
    subQuestion: SubQuestion
) {
    val label = QuestionPresentationLookups.subQuestionLabel(index, subQuestion.label)
    val questionText = QuestionPresentationLookups.displayQuestionText(subQuestion.questionText)

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "$label $questionText",
        fontSize = 14.sp
    )

    subQuestion.marks?.let { marks ->
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Marks: ${QuestionPresentationLookups.formatMarks(marks)}",
            fontSize = 12.sp
        )
    }

    subQuestion.formulaLatex?.trim()?.takeIf { it.isNotBlank() }?.let { formula ->
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Formula: $formula",
            fontSize = 12.sp
        )
    }

    subQuestion.options.orEmpty().takeIf { it.isNotEmpty() }?.let { options ->
        Spacer(modifier = Modifier.height(4.dp))
        options.forEachIndexed { optionIndex, option ->
            Text(
                text = "${optionIndex + 1}. ${option.trim()}",
                fontSize = 12.sp
            )
        }
    }

    subQuestion.diagramDescription?.trim()?.takeIf { it.isNotBlank() }?.let { description ->
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Diagram: $description",
            fontSize = 12.sp
        )
    }

    subQuestion.diagramReference?.trim()?.takeIf { it.isNotBlank() }?.let { reference ->
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Diagram reference: $reference",
            fontSize = 12.sp
        )
    }

    subQuestion.mathBlocks?.takeIf { !it.isJsonNull }?.let { mathBlocks ->
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Math blocks: ${QuestionPresentationLookups.formatJsonContent(mathBlocks)}",
            fontSize = 12.sp
        )
    }

    if (subQuestion.diagramRequired == true) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Diagram/chart required",
            fontSize = 12.sp
        )
    }
}
