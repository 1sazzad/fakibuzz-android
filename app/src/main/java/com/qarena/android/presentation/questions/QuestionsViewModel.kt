package com.qarena.android.presentation.questions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.data.remote.dto.QuestionResponse
import com.qarena.android.data.repository.QuestionRepository
import kotlinx.coroutines.launch

class QuestionsViewModel : ViewModel() {

    private val questionRepository = QuestionRepository()

    var questions by mutableStateOf<List<QuestionResponse>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var selectedSubjectCode by mutableStateOf<String?>(null)
        private set

    fun loadQuestions(subjectCode: String) {
        viewModelScope.launch {
            val trimmedSubjectCode = subjectCode.trim()

            selectedSubjectCode = trimmedSubjectCode
            isLoading = true
            errorMessage = null

            val result = questionRepository.getQuestions(trimmedSubjectCode)

            result
                .onSuccess { questionList ->
                    questions = questionList
                }
                .onFailure { exception ->
                    errorMessage = exception.message ?: "Failed to load questions"
                }

            isLoading = false
        }
    }
}
