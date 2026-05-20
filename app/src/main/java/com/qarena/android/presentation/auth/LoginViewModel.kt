package com.qarena.android.presentation.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.core.session.SessionManager
import com.qarena.android.data.remote.dto.UserResponse
import com.qarena.android.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var loginSuccess by mutableStateOf(false)
        private set

    var accessToken by mutableStateOf<String?>(null)
        private set

    var currentUser by mutableStateOf<UserResponse?>(null)
        private set

    var userLoadError by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    fun login() {
        viewModelScope.launch {
            val trimmedEmail = email.trim()

            if (trimmedEmail.isBlank()) {
                errorMessage = "Email is required"
                loginSuccess = false
                return@launch
            }

            if (password.isBlank()) {
                errorMessage = "Password is required"
                loginSuccess = false
                return@launch
            }

            isLoading = true
            errorMessage = null
            loginSuccess = false
            userLoadError = null
            currentUser = null

            val result = authRepository.login(
                email = trimmedEmail,
                password = password
            )

            result
                .onSuccess { response ->
                    val token = response.accessToken

                    if (token.isNullOrBlank()) {
                        errorMessage = "Login failed: token missing"
                        loginSuccess = false
                    } else {
                        accessToken = token
                        Log.d("LoginViewModel", "Login successful, token received")

                        val userResult = authRepository.getCurrentUser(token)

                        userResult
                            .onSuccess { userResponse ->
                                SessionManager.saveSession(
                                    token = token,
                                    email = userResponse.email,
                                    role = userResponse.role,
                                    userId = userResponse.id
                                )
                                currentUser = userResponse
                                loginSuccess = true
                                Log.d("LoginViewModel", "Current user loaded")
                            }
                            .onFailure { exception ->
                                errorMessage = "Login succeeded, but failed to load user"
                                userLoadError = exception.message
                                loginSuccess = false
                            }
                    }
                }
                .onFailure { exception ->
                    errorMessage = exception.message ?: "Login failed"
                    loginSuccess = false
                }

            isLoading = false
        }
    }
}
