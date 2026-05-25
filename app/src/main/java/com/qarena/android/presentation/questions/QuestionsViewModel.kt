package com.qarena.android.presentation.questions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.data.remote.dto.QuestionResponse
import com.qarena.android.data.repository.QuestionLoadResult
import com.qarena.android.data.repository.QuestionRepository
import com.qarena.android.model.Subject
import com.qarena.android.util.PaperTypeLookups
import com.qarena.android.util.SubjectLookups
import com.qarena.android.data.repository.SubjectRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class QuestionsViewModel : ViewModel() {

    private val questionRepository = QuestionRepository()
    private val subjectRepository = SubjectRepository()
    private var loadJob: Job? = null

    var questions by mutableStateOf<List<QuestionResponse>>(emptyList())
        private set

    var totalCount by mutableStateOf(0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var selectedSubjectCode by mutableStateOf<String?>(null)
        private set

    var subject by mutableStateOf<Subject?>(null)
        private set

    var supportedPaperTypes by mutableStateOf<List<String>>(emptyList())
        private set

    var selectedPaperType by mutableStateOf<String?>(null)
        private set

    fun loadQuestions(subjectCode: String, paperType: String? = null) {
        loadJob?.cancel()
        questions = emptyList()
        totalCount = 0
        loadJob = viewModelScope.launch {
            val trimmedSubjectCode = subjectCode.trim()

            if (trimmedSubjectCode.isBlank()) {
                errorMessage = "Subject code is required"
                questions = emptyList()
                selectedSubjectCode = null
                isLoading = false
                return@launch
            }

            val subjectChanged = selectedSubjectCode != trimmedSubjectCode

            selectedSubjectCode = trimmedSubjectCode
            if (subjectChanged) {
                subject = null
                supportedPaperTypes = emptyList()
                selectedPaperType = null
                questions = emptyList()
            }

            isLoading = true
            errorMessage = null

            ensureSubjectMetadata(trimmedSubjectCode)

            val effectivePaperType = PaperTypeLookups.resolveSelectedPaperType(
                preferredPaperType = paperType ?: selectedPaperType,
                supportedPaperTypes = supportedPaperTypes
            )
            selectedPaperType = effectivePaperType

            val result = questionRepository.getQuestions(
                subjectCode = trimmedSubjectCode,
                paperType = effectivePaperType,
                debugScreenName = "Questions",
                subject = subject
            )

            result
                .onSuccess { questionLoadResult ->
                    questions = questionLoadResult.questions
                    totalCount = questionLoadResult.totalCount
                }
                .onFailure { exception ->
                    errorMessage = exception.message ?: "Failed to load questions"
                    totalCount = 0
                }

            isLoading = false
        }
    }

    fun selectPaperType(subjectCode: String, paperType: String) {
        loadQuestions(subjectCode, paperType)
    }

    private suspend fun ensureSubjectMetadata(subjectCode: String) {
        if (subject != null && selectedSubjectCode == subjectCode) {
            return
        }

        val result = subjectRepository.getSubjectOverview(
            subjectCode = subjectCode,
            debugScreenName = "Questions",
            subject = subject
        )

        result
            .onSuccess { overview ->
                val subjectMetadata = overview.subject?.let { with(SubjectLookups) { it.toSubject() } }
                subject = subjectMetadata
                supportedPaperTypes = (PaperTypeLookups.normalizeSupportedPaperTypes(
                    subjectMetadata?.supportedPaperTypes ?: overview.subject?.supportedPaperTypes
                ) + PaperTypeLookups.allPaperTypes()).distinct()
                selectedPaperType = PaperTypeLookups.resolveSelectedPaperType(
                    preferredPaperType = selectedPaperType,
                    supportedPaperTypes = supportedPaperTypes
                )
            }
            .onFailure {
                if (subject == null) {
                    supportedPaperTypes = emptyList()
                    selectedPaperType = null
                }
            }
    }
}
