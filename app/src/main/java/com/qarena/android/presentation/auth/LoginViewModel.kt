package com.qarena.android.presentation.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.core.session.SessionManager
import com.qarena.android.core.session.TokenStorage
import com.qarena.android.core.session.isProfileComplete
import com.qarena.android.data.remote.dto.UserResponse
import com.qarena.android.data.repository.AuthRepository
import kotlinx.coroutines.launch

sealed interface LoginRouteState {
    data object Idle : LoginRouteState
    data object Loading : LoginRouteState
    data object SuccessProfileComplete : LoginRouteState
    data object SuccessProfileIncomplete : LoginRouteState
    data class Error(val message: String) : LoginRouteState
}

object LoginInputValidator {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    fun validate(email: String, password: String): String? {
        val trimmedEmail = email.trim()

        return when {
            trimmedEmail.isBlank() -> "Email is required"
            !emailRegex.matches(trimmedEmail) -> "Enter a valid email address"
            password.isBlank() -> "Password is required"
            else -> null
        }
    }
}

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val tokenStorage = TokenStorage(application)

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

    var loginRouteState by mutableStateOf<LoginRouteState>(LoginRouteState.Idle)
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
            val validationError = LoginInputValidator.validate(email, password)

            if (validationError != null) {
                errorMessage = validationError
                loginSuccess = false
                loginRouteState = LoginRouteState.Error(validationError)
                return@launch
            }

            isLoading = true
            errorMessage = null
            loginSuccess = false
            loginRouteState = LoginRouteState.Loading
            userLoadError = null
            currentUser = null

            val result = authRepository.login(
                email = trimmedEmail,
                password = password
            )

            result
                .onSuccess { response ->
                    val token = response.access_token

                    if (token.isBlank()) {
                        errorMessage = "Login failed: token missing"
                        loginSuccess = false
                        loginRouteState = LoginRouteState.Error("Login failed: token missing")
                    } else {
                        accessToken = token
                        SessionManager.saveSession(
                            token = token,
                            email = null,
                            role = response.role,
                            userId = null
                        )
                        tokenStorage.saveAccessToken(token)

                        val userResult = authRepository.getCurrentUser(token)

                        userResult
                            .onSuccess { userResponse ->
                                SessionManager.saveSession(
                                    tokenStorage = tokenStorage,
                                    token = token,
                                    email = userResponse.email,
                                    role = userResponse.role,
                                    userId = userResponse.id
                                )
                                currentUser = userResponse
                                loginSuccess = true
                                loginRouteState = if (userResponse.isProfileComplete) {
                                    LoginRouteState.SuccessProfileComplete
                                } else {
                                    LoginRouteState.SuccessProfileIncomplete
                                }
                            }
                            .onFailure { exception ->
                                errorMessage = "Login succeeded, but failed to load user"
                                userLoadError = exception.message
                                loginSuccess = false
                                SessionManager.clearSession(tokenStorage)
                                loginRouteState = LoginRouteState.Error(
                                    exception.message ?: "Login succeeded, but failed to load user"
                                )
                            }
                    }
                }
                .onFailure { exception ->
                    errorMessage = exception.message ?: "Login failed"
                    loginSuccess = false
                    loginRouteState = LoginRouteState.Error(errorMessage ?: "Login failed")
                }

            isLoading = false
        }
    }

    fun resetLoginRouteState() {
        loginRouteState = LoginRouteState.Idle
    }
}
