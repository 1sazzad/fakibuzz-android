package com.qarena.android.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.data.remote.dto.ResetPasswordResponse
import com.qarena.android.data.repository.AuthRepository
import kotlinx.coroutines.launch

sealed interface ResetPasswordUiState {
    data object Idle : ResetPasswordUiState
    data object Loading : ResetPasswordUiState
    data class Success(val response: ResetPasswordResponse) : ResetPasswordUiState
    data class Error(val message: String) : ResetPasswordUiState
}

class ResetPasswordViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    var token by mutableStateOf("")
        private set

    var newPassword by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var resetPasswordState by mutableStateOf<ResetPasswordUiState>(ResetPasswordUiState.Idle)
        private set

    fun onTokenChange(newToken: String) {
        token = newToken
        resetPasswordState = ResetPasswordUiState.Idle
    }

    fun onNewPasswordChange(password: String) {
        newPassword = password
        resetPasswordState = ResetPasswordUiState.Idle
    }

    fun onConfirmPasswordChange(password: String) {
        confirmPassword = password
        resetPasswordState = ResetPasswordUiState.Idle
    }

    fun resetPassword() {
        viewModelScope.launch {
            val validationMessage = validateInput()

            if (validationMessage != null) {
                resetPasswordState = ResetPasswordUiState.Error(validationMessage)
                return@launch
            }

            resetPasswordState = ResetPasswordUiState.Loading

            val result = authRepository.resetPassword(
                token = token,
                newPassword = newPassword
            )

            result
                .onSuccess { response ->
                    resetPasswordState = ResetPasswordUiState.Success(response)
                }
                .onFailure { exception ->
                    resetPasswordState = ResetPasswordUiState.Error(
                        exception.message ?: "Failed to reset password"
                    )
                }
        }
    }

    fun resetState() {
        resetPasswordState = ResetPasswordUiState.Idle
    }

    private fun validateInput(): String? {
        return when {
            token.trim().isBlank() -> "Reset token is required"
            newPassword.isBlank() -> "New password is required"
            confirmPassword.isBlank() -> "Confirm password is required"
            newPassword != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }
}
