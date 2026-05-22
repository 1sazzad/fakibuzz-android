package com.qarena.android.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.data.remote.dto.ForgotPasswordResponse
import com.qarena.android.data.repository.AuthRepository
import kotlinx.coroutines.launch

sealed interface ForgotPasswordUiState {
    data object Idle : ForgotPasswordUiState
    data object Loading : ForgotPasswordUiState
    data class Success(val response: ForgotPasswordResponse) : ForgotPasswordUiState
    data class Error(val message: String) : ForgotPasswordUiState
}

class ForgotPasswordViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    var email by mutableStateOf("")
        private set

    var forgotPasswordState by mutableStateOf<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
        private set

    fun onEmailChange(newEmail: String) {
        email = newEmail
        forgotPasswordState = ForgotPasswordUiState.Idle
    }

    fun forgotPassword() {
        viewModelScope.launch {
            val trimmedEmail = email.trim()

            if (trimmedEmail.isBlank()) {
                forgotPasswordState = ForgotPasswordUiState.Error("Email is required")
                return@launch
            }

            forgotPasswordState = ForgotPasswordUiState.Loading

            val result = authRepository.forgotPassword(trimmedEmail)

            result
                .onSuccess { response ->
                    forgotPasswordState = ForgotPasswordUiState.Success(response)
                }
                .onFailure { exception ->
                    forgotPasswordState = ForgotPasswordUiState.Error(
                        exception.message ?: "Failed to send reset instructions"
                    )
                }
        }
    }

    fun resetState() {
        forgotPasswordState = ForgotPasswordUiState.Idle
    }
}
