package com.qarena.android.presentation.subjects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.data.remote.dto.SubjectResponse
import com.qarena.android.data.repository.SubjectRepository
import kotlinx.coroutines.launch

class SubjectsViewModel : ViewModel() {

    private val subjectRepository = SubjectRepository()

    var subjects by mutableStateOf<List<SubjectResponse>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadSubjects() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = subjectRepository.getSubjects()

            result
                .onSuccess { subjectList ->
                    subjects = subjectList
                }
                .onFailure { exception ->
                    errorMessage = exception.message ?: "Failed to load subjects"
                }

            isLoading = false
        }
    }
}
