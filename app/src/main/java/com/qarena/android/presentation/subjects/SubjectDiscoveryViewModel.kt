package com.qarena.android.presentation.subjects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.data.remote.dto.SubjectOverviewResponse
import com.qarena.android.data.repository.QuestionLoadResult
import com.qarena.android.data.repository.QuestionRepository
import com.qarena.android.data.repository.SubjectRepository
import com.qarena.android.model.Subject
import com.qarena.android.util.PaperTypeLookups
import com.qarena.android.util.SubjectLookups
import kotlinx.coroutines.launch

class SubjectDiscoveryViewModel : ViewModel() {

    private val subjectRepository = SubjectRepository()
    private val questionRepository = QuestionRepository()

    var searchQuery by mutableStateOf("")
        private set

    var publishedSubjects by mutableStateOf<List<Subject>>(emptyList())
        private set

    var selectedSubject by mutableStateOf<Subject?>(null)
        private set

    var selectedSubjectOverview by mutableStateOf<SubjectOverviewResponse?>(null)
        private set

    var selectedPaperType by mutableStateOf<String?>(null)
        private set

    var questions by mutableStateOf<List<com.qarena.android.data.remote.dto.QuestionResponse>>(emptyList())
        private set

    var totalCount by mutableStateOf(0)
        private set

    var isLoadingSubjects by mutableStateOf(false)
        private set

    var isLoadingQuestions by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var publishedSubjectsErrorMessage by mutableStateOf<String?>(null)
        private set

    fun bootstrap(initialSubjectCode: String? = null) {
        viewModelScope.launch {
            if (!initialSubjectCode.isNullOrBlank()) {
                searchQuery = initialSubjectCode
                searchPublishedSubjects(initialSubjectCode, autoSelect = true)
            } else {
                loadPublishedSubjects()
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    fun loadPublishedSubjects() {
        viewModelScope.launch {
            isLoadingSubjects = true
            publishedSubjectsErrorMessage = null

            val result = subjectRepository.getSubjects()
            result
                .onSuccess { subjects ->
                    publishedSubjects = subjects
                }
                .onFailure { exception ->
                    publishedSubjectsErrorMessage = exception.message ?: "Failed to load published subjects"
                }

            isLoadingSubjects = false
        }
    }

    fun searchPublishedSubjects(query: String, autoSelect: Boolean = false) {
        viewModelScope.launch {
            val trimmedQuery = query.trim()

            if (trimmedQuery.isBlank()) {
                loadPublishedSubjects()
                return@launch
            }

            isLoadingSubjects = true
            publishedSubjectsErrorMessage = null

            val result = subjectRepository.searchSubjects(trimmedQuery)
            result
                .onSuccess { subjects ->
                    publishedSubjects = subjects

                    if (autoSelect) {
                        val matchedSubject = subjects.firstOrNull {
                            it.subjectCode.equals(trimmedQuery, ignoreCase = true)
                        } ?: subjects.firstOrNull()

                        if (matchedSubject != null) {
                            selectSubject(matchedSubject)
                        }
                    }
                }
                .onFailure { exception ->
                    publishedSubjectsErrorMessage = exception.message ?: "Failed to search published subjects"
                }

            isLoadingSubjects = false
        }
    }

    fun selectSubject(subject: Subject) {
        viewModelScope.launch {
            val trimmedSubjectCode = subject.subjectCode.trim()

            if (trimmedSubjectCode.isBlank()) {
                errorMessage = "Subject code is required"
                return@launch
            }

            selectedSubject = subject
            selectedSubjectOverview = null
            questions = emptyList()
            totalCount = 0
            errorMessage = null
            selectedPaperType = resolveDefaultPaperType(subject.supportedPaperTypes)

            loadSelectedSubjectData(trimmedSubjectCode, subject)
        }
    }

    fun selectPaperType(subjectCode: String, paperType: String) {
        val normalizedPaperType = PaperTypeLookups.normalizePaperType(paperType) ?: return

        selectedPaperType = normalizedPaperType
        questions = emptyList()
        totalCount = 0
        errorMessage = null

        viewModelScope.launch {
            isLoadingQuestions = true
            loadQuestions(subjectCode, selectedSubject)
            isLoadingQuestions = false
        }
    }

    fun retrySelectedSubjectLoad() {
        val subject = selectedSubject ?: return
        viewModelScope.launch {
            loadSelectedSubjectData(subject.subjectCode, subject)
        }
    }

    private suspend fun loadSelectedSubjectData(subjectCode: String, subject: Subject) {
        isLoadingQuestions = true
        errorMessage = null

        val overviewResult = subjectRepository.getSubjectOverview(
            subjectCode = subjectCode,
            subject = subject,
            debugScreenName = "SubjectDiscovery"
        )

        overviewResult
            .onSuccess { overview ->
                selectedSubjectOverview = overview
                val subjectFromOverview = overview.subject?.let { with(SubjectLookups) { it.toSubject() } }
                if (subjectFromOverview != null) {
                    selectedSubject = subjectFromOverview
                    selectedPaperType = resolveDefaultPaperType(subjectFromOverview.supportedPaperTypes)
                }
            }
            .onFailure { exception ->
                errorMessage = exception.message ?: "Failed to load subject overview"
                selectedSubjectOverview = null
            }

        loadQuestions(subjectCode, selectedSubject)
        isLoadingQuestions = false
    }

    private suspend fun loadQuestions(subjectCode: String, subject: Subject?) {
        val effectivePaperType = PaperTypeLookups.resolveSelectedPaperType(
            preferredPaperType = selectedPaperType,
            supportedPaperTypes = subject?.supportedPaperTypes
        ) ?: PaperTypeLookups.CQ

        selectedPaperType = effectivePaperType

        val result: Result<QuestionLoadResult> = questionRepository.getQuestions(
            subjectCode = subjectCode,
            paperType = effectivePaperType,
            debugScreenName = "SubjectDiscovery",
            subject = subject
        )

        result
            .onSuccess { questionLoadResult ->
                questions = questionLoadResult.questions
                totalCount = questionLoadResult.totalCount
            }
            .onFailure { exception ->
                errorMessage = exception.message ?: "Failed to load questions"
                questions = emptyList()
                totalCount = 0
            }
    }

    private fun resolveDefaultPaperType(supportedPaperTypes: List<String>?): String? {
        val normalized = PaperTypeLookups.normalizeSupportedPaperTypes(supportedPaperTypes)
        return normalized.firstOrNull() ?: PaperTypeLookups.CQ
    }
}