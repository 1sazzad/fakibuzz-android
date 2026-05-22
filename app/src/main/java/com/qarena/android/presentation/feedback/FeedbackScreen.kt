package com.qarena.android.presentation.feedback

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.core.analytics.AnalyticsTracker
import com.qarena.android.data.remote.dto.FeedbackRequest
import com.qarena.android.data.remote.dto.FeedbackResponse

@Composable
fun FeedbackScreen(
    feedbackViewModel: FeedbackViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var localErrorMessage by remember { mutableStateOf<String?>(null) }
    val submitState = feedbackViewModel.submitFeedbackState
    val publicFeedbackState = feedbackViewModel.publicFeedbackState

    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreen(
            screenName = "Feedback",
            path = "/android/feedback"
        )
        feedbackViewModel.getPublicFeedback(limit = 10)
    }

    LaunchedEffect(submitState) {
        if (submitState is FeedbackSubmitUiState.Success) {
            message = ""
            localErrorMessage = null
            feedbackViewModel.getPublicFeedback(limit = 10)
        }
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
                text = "Feedback",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            FeedbackForm(
                name = name,
                onNameChange = {
                    name = it
                    localErrorMessage = null
                    feedbackViewModel.resetSubmitFeedbackState()
                },
                email = email,
                onEmailChange = {
                    email = it
                    localErrorMessage = null
                    feedbackViewModel.resetSubmitFeedbackState()
                },
                rating = rating,
                onRatingChange = {
                    rating = it
                    localErrorMessage = null
                    feedbackViewModel.resetSubmitFeedbackState()
                },
                message = message,
                onMessageChange = {
                    message = it
                    localErrorMessage = null
                    feedbackViewModel.resetSubmitFeedbackState()
                },
                submitState = submitState,
                localErrorMessage = localErrorMessage,
                onSubmit = {
                    val parsedRating = rating.trim().takeIf { it.isNotBlank() }?.toIntOrNull()
                    localErrorMessage = validateFeedbackInput(
                        message = message,
                        ratingText = rating,
                        parsedRating = parsedRating
                    )

                    if (localErrorMessage == null) {
                        feedbackViewModel.submitFeedback(
                            FeedbackRequest(
                                name = name.trim().takeIf { it.isNotBlank() },
                                email = email.trim().takeIf { it.isNotBlank() },
                                rating = parsedRating,
                                message = message.trim(),
                                pageUrl = "android://feedback",
                                feedbackType = "general"
                            )
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Public Feedback",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            PublicFeedbackContent(publicFeedbackState = publicFeedbackState)
        }
    }
}

@Composable
private fun FeedbackForm(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    rating: String,
    onRatingChange: (String) -> Unit,
    message: String,
    onMessageChange: (String) -> Unit,
    submitState: FeedbackSubmitUiState,
    localErrorMessage: String?,
    onSubmit: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Submit Feedback",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Name") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Email") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = rating,
                onValueChange = onRatingChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Rating 1-5") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Message") },
                minLines = 4
            )

            FeedbackSubmitMessage(
                localErrorMessage = localErrorMessage,
                submitState = submitState
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = submitState !is FeedbackSubmitUiState.Loading
            ) {
                Text(
                    text = if (submitState is FeedbackSubmitUiState.Loading) {
                        "Submitting..."
                    } else {
                        "Submit Feedback"
                    }
                )
            }
        }
    }
}

@Composable
private fun FeedbackSubmitMessage(
    localErrorMessage: String?,
    submitState: FeedbackSubmitUiState
) {
    val message = when {
        localErrorMessage != null -> localErrorMessage
        submitState is FeedbackSubmitUiState.Error -> submitState.message
        submitState is FeedbackSubmitUiState.Success -> {
            submitState.response.message ?: "Feedback submitted"
        }
        else -> null
    }

    val isError = localErrorMessage != null || submitState is FeedbackSubmitUiState.Error

    message?.let {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = it,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            fontSize = 14.sp
        )
    }
}

@Composable
private fun PublicFeedbackContent(
    publicFeedbackState: PublicFeedbackUiState
) {
    when (publicFeedbackState) {
        PublicFeedbackUiState.Idle,
        PublicFeedbackUiState.Loading -> {
            Text(text = "Loading public feedback...")
        }

        is PublicFeedbackUiState.Error -> {
            Text(
                text = publicFeedbackState.message,
                color = MaterialTheme.colorScheme.error
            )
        }

        is PublicFeedbackUiState.Success -> {
            if (publicFeedbackState.response.feedback.isEmpty()) {
                Text(text = "No public feedback yet")
            } else {
                publicFeedbackState.response.feedback.forEach { feedback ->
                    FeedbackCard(feedback = feedback)
                }
            }
        }
    }
}

@Composable
private fun FeedbackCard(
    feedback: FeedbackResponse
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = feedback.name?.takeIf { it.isNotBlank() } ?: "Student",
                fontWeight = FontWeight.Bold
            )

            feedback.rating?.let { rating ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Rating: $rating/5",
                    fontSize = 14.sp
                )
            }

            feedback.message?.takeIf { it.isNotBlank() }?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = message)
            }

            feedback.pageUrl?.takeIf { it.isNotBlank() }?.let { pageUrl ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = pageUrl,
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun validateFeedbackInput(
    message: String,
    ratingText: String,
    parsedRating: Int?
): String? {
    val trimmedMessage = message.trim()
    val trimmedRating = ratingText.trim()

    return when {
        trimmedMessage.isBlank() -> "Message is required"
        trimmedMessage.length > MAX_FEEDBACK_MESSAGE_LENGTH -> {
            "Message must be 2000 characters or fewer"
        }
        trimmedRating.isNotBlank() && parsedRating == null -> "Rating must be a number from 1 to 5"
        parsedRating != null && parsedRating !in 1..5 -> "Rating must be between 1 and 5"
        else -> null
    }
}

private const val MAX_FEEDBACK_MESSAGE_LENGTH = 2000
