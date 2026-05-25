package com.qarena.android.presentation.subjects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.qarena.android.data.remote.dto.QuestionResponse
import com.qarena.android.data.remote.dto.SubjectAnalysisResponse
import com.qarena.android.data.repository.SubjectRepository
import com.qarena.android.data.repository.QuestionRepository
import com.qarena.android.model.Subject
import com.qarena.android.util.PaperTypeLookups
import com.qarena.android.util.SubjectLookups
import kotlinx.coroutines.launch

sealed interface SubjectAnalysisUiState {
    data object Idle : SubjectAnalysisUiState
    data object Loading : SubjectAnalysisUiState
    data class Success(val analysis: SubjectAnalysisResponse) : SubjectAnalysisUiState
    data class Error(val message: String) : SubjectAnalysisUiState
}

class SubjectAnalysisViewModel : ViewModel() {

    private val subjectRepository = SubjectRepository()
    private val questionRepository = QuestionRepository()

    var analysisState by mutableStateOf<SubjectAnalysisUiState>(SubjectAnalysisUiState.Idle)
        private set

    var subject by mutableStateOf<Subject?>(null)
        private set

    var supportedPaperTypes by mutableStateOf<List<String>>(emptyList())
        private set

    var selectedPaperType by mutableStateOf<String?>(null)
        private set

    fun loadSubjectAnalysis(subjectCode: String, paperType: String? = null) {
        viewModelScope.launch {
            val trimmedSubjectCode = subjectCode.trim()

            if (trimmedSubjectCode.isBlank()) {
                analysisState = SubjectAnalysisUiState.Error("Subject code is required")
                return@launch
            }

            analysisState = SubjectAnalysisUiState.Loading

            loadSubjectMetadata(trimmedSubjectCode)

            val effectivePaperType = PaperTypeLookups.resolveSelectedPaperType(
                preferredPaperType = paperType ?: selectedPaperType,
                supportedPaperTypes = supportedPaperTypes
            )
            selectedPaperType = effectivePaperType

            val result = subjectRepository.getSubjectAnalysis(
                subjectCode = trimmedSubjectCode,
                paperType = effectivePaperType,
                debugScreenName = "SubjectAnalysis",
                subject = subject
            )

            result
                .onSuccess { analysis ->
                    analysisState = SubjectAnalysisUiState.Success(analysis)
                }
                .onFailure { exception ->
                    val fallback = buildFallbackAnalysis(trimmedSubjectCode, effectivePaperType)
                    fallback
                        .onSuccess { analysis ->
                            analysisState = SubjectAnalysisUiState.Success(analysis)
                        }
                        .onFailure {
                            analysisState = SubjectAnalysisUiState.Error(
                                exception.message ?: it.message ?: "Failed to load subject analysis"
                            )
                        }
                }
        }
    }

    fun selectPaperType(subjectCode: String, paperType: String) {
        loadSubjectAnalysis(subjectCode, paperType)
    }

    private suspend fun loadSubjectMetadata(subjectCode: String) {
        if (subject != null && subject?.subjectCode == subjectCode) {
            return
        }

        val result = subjectRepository.getSubjectOverview(
            subjectCode = subjectCode,
            debugScreenName = "SubjectAnalysis",
            subject = subject
        )

        result
            .onSuccess { overview ->
                val subjectMetadata = overview.subject?.let { with(SubjectLookups) { it.toSubject() } }
                subject = subjectMetadata
                supportedPaperTypes = PaperTypeLookups.normalizeSupportedPaperTypes(
                    subjectMetadata?.supportedPaperTypes ?: overview.subject?.supportedPaperTypes
                )
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

    private suspend fun buildFallbackAnalysis(
        subjectCode: String,
        paperType: String?
    ): Result<SubjectAnalysisResponse> {
        val effectivePaperType = paperType ?: PaperTypeLookups.CQ
        val result = questionRepository.getQuestions(subjectCode, effectivePaperType)

        return result.map { questionLoadResult ->
            val questions = questionLoadResult.questions
            SubjectAnalysisResponse(
                subjectCode = subjectCode,
                subjectName = subject?.subjectName,
                totalQuestions = questions.size,
                topics = buildTopicSummary(questions, effectivePaperType),
                marks = buildMarksSummary(questions),
                years = buildYearSummary(questions),
                samples = buildSamples(questions),
                summary = "Computed from questions API fallback",
                message = "Using questions API fallback for topic analysis",
                status = "fallback"
            )
        }
    }

    private fun buildTopicSummary(questions: List<QuestionResponse>, paperType: String): JsonArray {
        val topics = questions
            .groupBy { it.topic?.trim().takeUnless { topic -> topic.isNullOrBlank() } ?: "Uncategorized" }
            .map { (topicName, topicQuestions) ->
                val totalMarks = topicQuestions.sumOf { it.marks ?: 0 }
                val averageMarks = if (topicQuestions.isNotEmpty()) totalMarks.toDouble() / topicQuestions.size else 0.0
                val years = topicQuestions.mapNotNull { it.year ?: it.examYear }.distinct().sorted()
                JsonObject().apply {
                    addProperty("topic", topicName)
                    addProperty("count", topicQuestions.size)
                    addProperty("question_count", topicQuestions.size)
                    addProperty("total_marks", totalMarks)
                    addProperty("average_marks", averageMarks)
                    addProperty("paper_type", paperType)
                    add("years", JsonArray().apply {
                        years.forEach { add(JsonPrimitive(it)) }
                    })
                }
            }
            .sortedByDescending { element -> element.get("count")?.asInt ?: 0 }

        return JsonArray().apply {
            topics.forEach { add(it) }
        }
    }

    private fun buildMarksSummary(questions: List<QuestionResponse>): JsonArray {
        return JsonArray().apply {
            questions
                .groupBy { it.marks ?: 0 }
                .toSortedMap()
                .forEach { (marks, groupedQuestions) ->
                    add(
                        JsonObject().apply {
                            addProperty("marks", marks)
                            addProperty("count", groupedQuestions.size)
                        }
                    )
                }
        }
    }

    private fun buildYearSummary(questions: List<QuestionResponse>): JsonArray {
        val years = questions.mapNotNull { it.year ?: it.examYear }.distinct().sortedDescending()

        return JsonArray().apply {
            years.forEach { add(JsonPrimitive(it)) }
        }
    }

    private fun buildSamples(questions: List<QuestionResponse>): JsonArray {
        return JsonArray().apply {
            questions.take(5).forEach { question ->
                add(
                    JsonObject().apply {
                        addProperty("question_text", question.questionText ?: "")
                        addProperty("topic", question.topic ?: "")
                        addProperty("year", question.year ?: question.examYear ?: 0)
                        addProperty("marks", question.marks ?: 0)
                    }
                )
            }
        }
    }
}
