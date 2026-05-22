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
import com.qarena.android.util.SuggestionLookups

@Composable
fun SubjectPredictionsScreen(
    subjectCode: String,
    onGetAnswerClick: (questionId: Int?, questionText: String, marks: Int?) -> Unit,
    subjectPredictionsViewModel: SubjectPredictionsViewModel = viewModel()
) {
    val predictionsState = subjectPredictionsViewModel.predictionsState

    LaunchedEffect(subjectCode) {
        AnalyticsTracker.trackScreen(
            screenName = "Predictions",
            path = "/android/subjects/$subjectCode/predictions",
            subjectCode = subjectCode
        )
        subjectPredictionsViewModel.loadSubjectPredictions(subjectCode)
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
                        subjectCode = subjectCode,
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
    onGetAnswerClick: (questionId: Int?, questionText: String, marks: Int?) -> Unit
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
                    onGetAnswerClick = onGetAnswerClick
                )
            }
        }
    }
}

@Composable
private fun PredictionCard(
    prediction: Suggestion,
    onGetAnswerClick: (questionId: Int?, questionText: String, marks: Int?) -> Unit
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
                        prediction.questionId,
                        prediction.questionText,
                        prediction.marks
                    )
                },
                enabled = prediction.questionText.isNotBlank()
            ) {
                Text(text = "Get Answer")
            }
        }
    }
}
