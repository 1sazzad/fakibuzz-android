package com.qarena.android.presentation.subjects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.data.remote.dto.QuestionResponse
import com.qarena.android.data.remote.dto.SubjectOverviewResponse
import com.qarena.android.data.repository.QuestionLoadResult
import com.qarena.android.data.repository.QuestionRepository
import com.qarena.android.data.repository.SubjectRepository
import com.qarena.android.model.Subject
import com.qarena.android.util.PaperTypeLookups
import com.qarena.android.util.SubjectLookups
import kotlinx.coroutines.launch

sealed interface SubjectOverviewUiState {
    data object Idle : SubjectOverviewUiState
    data object Loading : SubjectOverviewUiState
    data class Success(val overview: SubjectOverviewResponse) : SubjectOverviewUiState
    data class Error(val message: String) : SubjectOverviewUiState
}

sealed interface QuestionsUiState {
    data object Idle : QuestionsUiState
    data object Loading : QuestionsUiState
    data class Success(val questions: List<QuestionResponse>) : QuestionsUiState
    data class Error(val message: String) : QuestionsUiState
}

class SubjectOverviewViewModel : ViewModel() {

    private val subjectRepository = SubjectRepository()
    private val questionRepository = QuestionRepository()

    var overviewState by mutableStateOf<SubjectOverviewUiState>(SubjectOverviewUiState.Idle)
        private set

    var questionsState by mutableStateOf<QuestionsUiState>(QuestionsUiState.Idle)
        private set

    var subject by mutableStateOf<Subject?>(null)
        private set

    var supportedPaperTypes by mutableStateOf<List<String>>(emptyList())
        private set

    var selectedPaperType by mutableStateOf<String?>(null)
        private set

    var questions by mutableStateOf<List<QuestionResponse>>(emptyList())
        private set

    var totalCount by mutableStateOf(0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadSubjectData(subjectCode: String, paperType: String? = null) {
        isLoading = true
        errorMessage = null
        questionsState = QuestionsUiState.Loading
        viewModelScope.launch {
            val trimmedSubjectCode = subjectCode.trim()

            if (trimmedSubjectCode.isBlank()) {
                overviewState = SubjectOverviewUiState.Error("Subject code is required")
                errorMessage = "Subject code is required"
                isLoading = false
                return@launch
            }

            // Load Overview if not loaded or subject changed
            if (overviewState !is SubjectOverviewUiState.Success || subject?.subjectCode != trimmedSubjectCode) {
                overviewState = SubjectOverviewUiState.Loading
                val overviewResult = subjectRepository.getSubjectOverview(trimmedSubjectCode)
                
                overviewResult
                    .onSuccess { overview ->
                        val subjectMetadata = overview.subject?.let { with(SubjectLookups) { it.toSubject() } }
                        subject = subjectMetadata
                        supportedPaperTypes = (PaperTypeLookups.normalizeSupportedPaperTypes(
                            subjectMetadata?.supportedPaperTypes ?: overview.subject?.supportedPaperTypes
                        ) + PaperTypeLookups.allPaperTypes()).distinct()
                        overviewState = SubjectOverviewUiState.Success(overview)
                    }
                    .onFailure { exception ->
                        overviewState = SubjectOverviewUiState.Error(
                            exception.message ?: "Failed to load subject overview"
                        )
                        errorMessage = exception.message ?: "Failed to load subject overview"
                        isLoading = false
                        questionsState = QuestionsUiState.Error(errorMessage ?: "Failed to load subject overview")
                        questions = emptyList()
                        totalCount = 0
                        return@launch
                    }
            }

            // Determine paper type to load
            // IMPORTANT: If paperType is provided, we use it directly as the explicit selection.
            // This ensures clicking a chip actually switches the view.
            val effectivePaperType = if (paperType != null) {
                PaperTypeLookups.normalizePaperType(paperType)
            } else {
                PaperTypeLookups.resolveSelectedPaperType(
                    preferredPaperType = selectedPaperType,
                    supportedPaperTypes = supportedPaperTypes
                )
            }
            selectedPaperType = effectivePaperType

            // Load Questions
            questionsState = QuestionsUiState.Loading
            
            val questionsResult = questionRepository.getQuestions(
                subjectCode = trimmedSubjectCode,
                paperType = effectivePaperType,
                debugScreenName = "SubjectOverview",
                subject = subject
            )

            questionsResult
                .onSuccess { questionLoadResult ->
                    questions = questionLoadResult.questions
                    totalCount = questionLoadResult.totalCount
                    questionsState = QuestionsUiState.Success(questionLoadResult.questions)
                }
                .onFailure { exception ->
                    val message = exception.message ?: "Failed to load questions"
                    errorMessage = message
                    questionsState = QuestionsUiState.Error(message)
                    questions = emptyList()
                    totalCount = 0
                }

            isLoading = false
        }
    }

    fun selectPaperType(subjectCode: String, paperType: String) {
        loadSubjectData(subjectCode, paperType)
    }
}
