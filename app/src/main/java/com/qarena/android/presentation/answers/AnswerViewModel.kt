package com.qarena.android.presentation.answers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonElement
import com.qarena.android.data.remote.dto.GenerateAnswerRequest
import com.qarena.android.data.remote.dto.GenerateAnswerResponse
import com.qarena.android.data.remote.dto.JobResponse
import com.qarena.android.data.repository.AnswerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface AnswerUiState {
    data object Idle : AnswerUiState
    data object Loading : AnswerUiState
    data class Polling(
        val jobId: String,
        val status: String? = null,
        val message: String? = null,
        val progress: String? = null,
        val attempt: Int = 0
    ) : AnswerUiState

    data class Success(
        val answerText: String,
        val status: String? = null,
        val message: String? = null,
        val jobId: String? = null
    ) : AnswerUiState

    data class Error(val message: String) : AnswerUiState
}

sealed interface JobStatusUiState {
    data object Idle : JobStatusUiState
    data object Loading : JobStatusUiState
    data class Success(val job: JobResponse) : JobStatusUiState
    data class Error(val message: String) : JobStatusUiState
}

class AnswerViewModel : ViewModel() {

    private val answerRepository = AnswerRepository()
    private var pollingJob: Job? = null

    var answerState by mutableStateOf<AnswerUiState>(AnswerUiState.Idle)
        private set

    var jobStatusState by mutableStateOf<JobStatusUiState>(JobStatusUiState.Idle)
        private set

    fun generateAnswer(request: GenerateAnswerRequest) {
        cancelPolling()

        viewModelScope.launch {
            answerState = AnswerUiState.Loading

            val result = answerRepository.generateAnswer(request)

            result
                .onSuccess { response ->
                    val answerText = response.answerText()
                    val jobId = response.jobId?.takeIf { it.isNotBlank() }

                    if (!answerText.isNullOrBlank()) {
                        answerState = AnswerUiState.Success(
                            answerText = answerText,
                            status = response.status,
                            message = response.message,
                            jobId = jobId
                        )
                    } else if (jobId != null) {
                        pollJobUntilComplete(jobId)
                    } else {
                        answerState = AnswerUiState.Success(
                            answerText = response.message
                                ?: response.status
                                ?: "Answer generated, but no answer text was returned.",
                            status = response.status,
                            message = response.message
                        )
                    }
                }
                .onFailure { exception ->
                    answerState = AnswerUiState.Error(
                        exception.message ?: "Failed to generate answer"
                    )
                }
        }
    }

    fun pollJobUntilComplete(jobId: String) {
        cancelPolling()

        val trimmedJobId = jobId.trim()

        if (trimmedJobId.isBlank()) {
            answerState = AnswerUiState.Error("Job id is required")
            return
        }

        pollingJob = viewModelScope.launch {
            repeat(MAX_POLL_ATTEMPTS) { attempt ->
                answerState = AnswerUiState.Polling(
                    jobId = trimmedJobId,
                    message = "Checking job status...",
                    attempt = attempt + 1
                )

                val result = answerRepository.getJobStatus(trimmedJobId)

                result
                    .onSuccess { job ->
                        val normalizedStatus = job.status.normalizedStatus()

                        when (normalizedStatus) {
                            "completed", "success" -> {
                                val answerText = job.answerText()
                                answerState = AnswerUiState.Success(
                                    answerText = answerText
                                        ?: "Answer generation completed, but no answer text was returned.",
                                    status = job.status,
                                    message = job.message,
                                    jobId = job.jobId ?: trimmedJobId
                                )
                                return@launch
                            }

                            "failed", "error", "cancelled" -> {
                                answerState = AnswerUiState.Error(
                                    job.error
                                        ?: job.message
                                        ?: "Answer generation ${job.status ?: "failed"}."
                                )
                                return@launch
                            }

                            else -> {
                                answerState = AnswerUiState.Polling(
                                    jobId = job.jobId ?: trimmedJobId,
                                    status = job.status,
                                    message = job.message,
                                    progress = job.progress,
                                    attempt = attempt + 1
                                )
                            }
                        }
                    }
                    .onFailure { exception ->
                        answerState = AnswerUiState.Error(
                            exception.message ?: "Failed to load job status"
                        )
                        return@launch
                    }

                delay(POLL_DELAY_MILLIS)
            }

            answerState = AnswerUiState.Error(
                "Answer generation timed out. Please try again."
            )
        }
    }

    fun getJobStatus(jobId: String) {
        viewModelScope.launch {
            jobStatusState = JobStatusUiState.Loading

            val result = answerRepository.getJobStatus(jobId)

            result
                .onSuccess { response ->
                    jobStatusState = JobStatusUiState.Success(response)
                }
                .onFailure { exception ->
                    jobStatusState = JobStatusUiState.Error(
                        exception.message ?: "Failed to load job status"
                    )
                }
        }
    }

    fun resetAnswerState() {
        cancelPolling()
        answerState = AnswerUiState.Idle
    }

    fun resetJobStatusState() {
        jobStatusState = JobStatusUiState.Idle
    }

    override fun onCleared() {
        cancelPolling()
        super.onCleared()
    }

    private fun cancelPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun GenerateAnswerResponse.answerText(): String? {
        return answer?.takeIf { it.isNotBlank() }
    }

    private fun JobResponse.answerText(): String? {
        return answer?.takeIf { it.isNotBlank() }
            ?: result.extractAnswerText()
    }

    private fun JsonElement?.extractAnswerText(): String? {
        if (this == null || isJsonNull) {
            return null
        }

        if (isJsonPrimitive) {
            return asString.takeIf { it.isNotBlank() }
        }

        if (!isJsonObject) {
            return null
        }

        val json = asJsonObject
        return json.getStringOrNull("answer")
            ?: json.getStringOrNull("message")
            ?: json.get("result").extractAnswerText()
            ?: json.getStringOrNull("text")
    }

    private fun com.google.gson.JsonObject.getStringOrNull(key: String): String? {
        val value = get(key) ?: return null

        return if (value.isJsonPrimitive) {
            value.asString.takeIf { it.isNotBlank() }
        } else {
            null
        }
    }

    private fun String?.normalizedStatus(): String? {
        return this?.trim()?.lowercase()
    }

    private companion object {
        const val POLL_DELAY_MILLIS = 2_000L
        const val MAX_POLL_ATTEMPTS = 30
    }
}
