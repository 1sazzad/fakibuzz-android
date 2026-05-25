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
import com.qarena.android.model.displayLabel
import com.qarena.android.model.displaySubtitle
import com.qarena.android.presentation.common.AnswerPayload
import com.qarena.android.presentation.common.DiagramInfo
import com.qarena.android.presentation.common.DiagramRenderer
import com.qarena.android.presentation.common.toDiagramInfo
import com.qarena.android.presentation.navigation.SelectedSubjectNavArgs
import com.qarena.android.presentation.subjects.PaperTypeSelector
import com.qarena.android.util.QuestionPresentationLookups
import com.qarena.android.util.SuggestionLookups

@Composable
fun SubjectPredictionsScreen(
    selectedSubjectNavArgs: SelectedSubjectNavArgs,
    onGetAnswerClick: (AnswerPayload) -> Unit,
    subjectPredictionsViewModel: SubjectPredictionsViewModel = viewModel()
) {
    val predictionsState = subjectPredictionsViewModel.predictionsState
    val subject = subjectPredictionsViewModel.subject
    val supportedPaperTypes = subjectPredictionsViewModel.supportedPaperTypes
    val selectedPaperType = subjectPredictionsViewModel.selectedPaperType

    LaunchedEffect(selectedSubjectNavArgs.subjectCode, selectedSubjectNavArgs.paperType) {
        AnalyticsTracker.trackScreen(
            screenName = "Predictions",
            path = "/android/subjects/${selectedSubjectNavArgs.subjectCode}/predictions",
            subjectCode = selectedSubjectNavArgs.subjectCode
        )
        subjectPredictionsViewModel.loadSubjectPredictions(
            selectedSubjectNavArgs.subjectCode,
            paperType = selectedSubjectNavArgs.paperType
        )
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
                text = "Predictions",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = selectedSubjectNavArgs.subjectName?.takeIf { it.isNotBlank() } ?: subject?.displayLabel() ?: selectedSubjectNavArgs.subjectCode,
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
                        subjectPredictionsViewModel.selectPaperType(selectedSubjectNavArgs.subjectCode, paperType)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (predictionsState) {
                SubjectPredictionsUiState.Idle,
                SubjectPredictionsUiState.Loading -> {
                    Text(text = "Loading subject predictions...")
                }

                is SubjectPredictionsUiState.Error -> {
                    Text(
                        text = predictionsState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is SubjectPredictionsUiState.Success -> {
                    SubjectPredictionsContent(
                        predictions = predictionsState.predictions,
                        subjectCode = selectedSubjectNavArgs.subjectCode,
                        subjectAcademicLevel = subject?.academicLevel,
                        onGetAnswerClick = onGetAnswerClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectPredictionsContent(
    predictions: List<Suggestion>,
    subjectCode: String,
    subjectAcademicLevel: String?,
    onGetAnswerClick: (AnswerPayload) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Subject predictions",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subjectCode,
                fontSize = 16.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (predictions.isEmpty()) {
        Text(text = "No predictions found")
    } else {
        LazyColumn {
            items(predictions) { prediction ->
                PredictionCard(
                    prediction = prediction,
                    subjectCode = subjectCode,
                    subjectAcademicLevel = subjectAcademicLevel,
                    onGetAnswerClick = onGetAnswerClick
                )
            }
        }
    }
}

@Composable
private fun PredictionCard(
    prediction: Suggestion,
    subjectCode: String,
    subjectAcademicLevel: String?,
    onGetAnswerClick: (AnswerPayload) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = prediction.topic ?: prediction.questionId?.let { "Question #$it" } ?: "Predicted question",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            prediction.questionText.takeIf { it.isNotBlank() }?.let { questionText ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = questionText,
                    fontSize = 16.sp
                )
            }

            DiagramRenderer(diagramInfo = prediction.toDiagramInfo())

            prediction.marks?.let { marks ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Marks: $marks",
                    fontSize = 14.sp
                )
            }

            prediction.year?.let { year ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Year: $year",
                    fontSize = 14.sp
                )
            }

            SuggestionLookups.formatPredictionScore(prediction.predictionScore)?.let { score ->
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
                        AnswerPayload(
                            questionId = prediction.questionId,
                            questionText = prediction.questionText,
                            prompt = QuestionPresentationLookups.buildAnswerPrompt(
                            questionText = prediction.questionText,
                            diagramDescription = prediction.diagramDescription,
                            diagramReference = prediction.diagramReference,
                            diagramType = prediction.diagramType,
                            diagramSvg = prediction.diagramSvg,
                                diagramRequired = prediction.diagramRequired
                            ),
                            subjectCode = subjectCode,
                            academicLevel = subjectAcademicLevel,
                            paperType = prediction.paperType,
                            topic = prediction.topic,
                            marks = prediction.marks,
                            formulaLatex = prediction.formulaLatex,
                            formulaDisplay = prediction.formulaDisplay,
                            diagramRequired = prediction.diagramRequired,
                            diagramType = prediction.diagramType,
                            diagramSvg = prediction.diagramSvg,
                            diagramUrl = prediction.diagramUrl,
                            diagramReference = prediction.diagramReference,
                            diagramDescription = prediction.diagramDescription
                        )
                    )
                },
                enabled = prediction.questionText.isNotBlank() ||
                    prediction.diagramRequired == true ||
                    !prediction.diagramSvg.isNullOrBlank() ||
                    !prediction.diagramDescription.isNullOrBlank()
            ) {
                Text(text = "Get Answer")
            }
        }
    }
}
