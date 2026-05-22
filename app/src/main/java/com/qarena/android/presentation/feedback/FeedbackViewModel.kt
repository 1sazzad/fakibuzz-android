package com.qarena.android.presentation.feedback

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.data.remote.dto.AnalyticsVisitRequest
import com.qarena.android.data.remote.dto.AnalyticsVisitResponse
import com.qarena.android.data.remote.dto.FeedbackListResponse
import com.qarena.android.data.remote.dto.FeedbackRequest
import com.qarena.android.data.remote.dto.FeedbackResponse
import com.qarena.android.data.repository.FeedbackRepository
import kotlinx.coroutines.launch

sealed interface FeedbackSubmitUiState {
    data object Idle : FeedbackSubmitUiState
    data object Loading : FeedbackSubmitUiState
    data class Success(val response: FeedbackResponse) : FeedbackSubmitUiState
    data class Error(val message: String) : FeedbackSubmitUiState
}

sealed interface PublicFeedbackUiState {
    data object Idle : PublicFeedbackUiState
    data object Loading : PublicFeedbackUiState
    data class Success(val response: FeedbackListResponse) : PublicFeedbackUiState
    data class Error(val message: String) : PublicFeedbackUiState
}

sealed interface AnalyticsVisitUiState {
    data object Idle : AnalyticsVisitUiState
    data object Loading : AnalyticsVisitUiState
    data class Success(val response: AnalyticsVisitResponse) : AnalyticsVisitUiState
    data class Error(val message: String) : AnalyticsVisitUiState
}

class FeedbackViewModel : ViewModel() {

    private val feedbackRepository = FeedbackRepository()

    var submitFeedbackState by mutableStateOf<FeedbackSubmitUiState>(FeedbackSubmitUiState.Idle)
        private set

    var publicFeedbackState by mutableStateOf<PublicFeedbackUiState>(PublicFeedbackUiState.Idle)
        private set

    var analyticsVisitState by mutableStateOf<AnalyticsVisitUiState>(AnalyticsVisitUiState.Idle)
        private set

    fun submitFeedback(request: FeedbackRequest) {
        viewModelScope.launch {
            submitFeedbackState = FeedbackSubmitUiState.Loading

            val result = feedbackRepository.submitFeedback(request)

            result
                .onSuccess { response ->
                    submitFeedbackState = FeedbackSubmitUiState.Success(response)
                }
                .onFailure { exception ->
                    submitFeedbackState = FeedbackSubmitUiState.Error(
                        exception.message ?: "Failed to submit feedback"
                    )
                }
        }
    }

    fun getPublicFeedback(limit: Int = 10) {
        viewModelScope.launch {
            publicFeedbackState = PublicFeedbackUiState.Loading

            val result = feedbackRepository.getPublicFeedback(limit)

            result
                .onSuccess { response ->
                    publicFeedbackState = PublicFeedbackUiState.Success(response)
                }
                .onFailure { exception ->
                    publicFeedbackState = PublicFeedbackUiState.Error(
                        exception.message ?: "Failed to load feedback"
                    )
                }
        }
    }

    fun trackVisit(request: AnalyticsVisitRequest) {
        viewModelScope.launch {
            analyticsVisitState = AnalyticsVisitUiState.Loading

            val result = feedbackRepository.trackVisit(request)

            result
                .onSuccess { response ->
                    analyticsVisitState = AnalyticsVisitUiState.Success(response)
                }
                .onFailure { exception ->
                    analyticsVisitState = AnalyticsVisitUiState.Error(
                        exception.message ?: "Failed to track visit"
                    )
                }
        }
    }

    fun resetSubmitFeedbackState() {
        submitFeedbackState = FeedbackSubmitUiState.Idle
    }

    fun resetPublicFeedbackState() {
        publicFeedbackState = PublicFeedbackUiState.Idle
    }

    fun resetAnalyticsVisitState() {
        analyticsVisitState = AnalyticsVisitUiState.Idle
    }
}
