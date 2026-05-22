package com.qarena.android.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qarena.android.data.remote.ApiErrorParser
import com.qarena.android.data.remote.dto.MessageResponse
import com.qarena.android.data.remote.dto.RegisterResponse
import com.qarena.android.data.remote.dto.VerifyEmailRequest
import com.qarena.android.data.repository.AuthRepository
import com.qarena.android.data.repository.InstitutionRepository
import com.qarena.android.model.Department
import com.qarena.android.model.University
import com.qarena.android.util.AcademicProfile
import com.qarena.android.util.RegisterFormInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RegisterActionUiState {
    data object Idle : RegisterActionUiState
    data object Loading : RegisterActionUiState
    data class Success(val message: String? = null) : RegisterActionUiState
    data class Error(val message: String) : RegisterActionUiState
}

sealed interface RegisterLookupUiState<out T> {
    data object Idle : RegisterLookupUiState<Nothing>
    data object Loading : RegisterLookupUiState<Nothing>
    data class Success<T>(val items: List<T>) : RegisterLookupUiState<T>
    data class Error(val message: String) : RegisterLookupUiState<Nothing>
}

object RegisterInputValidator {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    private val bdPhoneRegex = Regex("^(01\\d{9}|8801\\d{9}|\\+8801\\d{9})$")

    fun validate(input: RegisterFormInput): String? {
        return when {
            input.fullName.trim().isBlank() -> "Full name is required"
            input.email.trim().isBlank() -> "Email is required"
            !emailRegex.matches(input.email.trim()) -> "Enter a valid email address"
            input.phone.trim().isBlank() -> "Phone number is required"
            !bdPhoneRegex.matches(input.phone.trim()) -> "Enter a valid Bangladesh phone number"
            input.password.length < 8 -> "Password must be at least 8 characters"
            !input.password.any { it.isLetter() } -> "Password must include at least one letter"
            !input.password.any { it.isDigit() } -> "Password must include at least one number"
            input.confirmPassword.isBlank() -> "Confirm password is required"
            input.password != input.confirmPassword -> "Passwords do not match"
            else -> AcademicProfile.validateRegistration(input)
        }
    }
}

class RegisterViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val institutionRepository: InstitutionRepository = InstitutionRepository()
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterActionUiState>(RegisterActionUiState.Idle)
    val registerState: StateFlow<RegisterActionUiState> = _registerState.asStateFlow()

    private val _universitiesState =
        MutableStateFlow<RegisterLookupUiState<University>>(RegisterLookupUiState.Idle)
    val universitiesState: StateFlow<RegisterLookupUiState<University>> =
        _universitiesState.asStateFlow()

    private val _departmentsState =
        MutableStateFlow<RegisterLookupUiState<Department>>(RegisterLookupUiState.Idle)
    val departmentsState: StateFlow<RegisterLookupUiState<Department>> =
        _departmentsState.asStateFlow()

    var verifyEmailState by mutableStateOf<RegisterActionUiState>(
        RegisterActionUiState.Idle
    )
        private set

    var resendVerificationEmailState by mutableStateOf<RegisterActionUiState>(
        RegisterActionUiState.Idle
    )
        private set

    var registerResponse by mutableStateOf<RegisterResponse?>(null)
        private set

    var verificationResponse by mutableStateOf<MessageResponse?>(null)
        private set

    var resendVerificationResponse by mutableStateOf<MessageResponse?>(null)
        private set

    fun loadUniversities() {
        viewModelScope.launch {
            _universitiesState.value = RegisterLookupUiState.Loading

            institutionRepository.getUniversities()
                .onSuccess { response ->
                    _universitiesState.value = RegisterLookupUiState.Success(response.universities)
                }
                .onFailure { exception ->
                    _universitiesState.value = RegisterLookupUiState.Error(
                        ApiErrorParser.messageForThrowable(exception)
                    )
                }
        }
    }

    fun loadDepartments(universityId: Int) {
        viewModelScope.launch {
            _departmentsState.value = RegisterLookupUiState.Loading

            institutionRepository.getDepartments(universityId)
                .onSuccess { response ->
                    _departmentsState.value = RegisterLookupUiState.Success(response.departments)
                }
                .onFailure { exception ->
                    _departmentsState.value = RegisterLookupUiState.Error(
                        ApiErrorParser.messageForThrowable(exception)
                    )
                }
        }
    }

    fun clearDepartments() {
        _departmentsState.value = RegisterLookupUiState.Idle
    }

    fun clearUniversities() {
        _universitiesState.value = RegisterLookupUiState.Idle
    }

    fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        academicLevel: String,
        universityId: Int? = null,
        departmentId: Int? = null,
        curriculum: String? = null,
        streamGroup: String? = null,
        classLevel: String? = null,
        program: String? = null,
        batchSession: String? = null
    ) {
        viewModelScope.launch {
            _registerState.value = RegisterActionUiState.Loading
            registerResponse = null

            val request = AcademicProfile.buildRegisterRequest(
                fullName = fullName,
                email = email,
                phone = phone,
                password = password,
                academicLevel = academicLevel,
                universityId = universityId,
                departmentId = departmentId,
                curriculum = curriculum,
                streamGroup = streamGroup,
                classLevel = classLevel,
                program = program,
                batchSession = batchSession,
                termsAccepted = true
            )

            authRepository.register(request)
                .onSuccess { response ->
                    registerResponse = response
                    _registerState.value = RegisterActionUiState.Success(
                        "Account created successfully. Please verify your email."
                    )
                }
                .onFailure { exception ->
                    _registerState.value = RegisterActionUiState.Error(
                        ApiErrorParser.messageForThrowable(exception)
                    )
                }
        }
    }

    fun verifyEmail(
        email: String? = null,
        code: String? = null,
        token: String? = null
    ) {
        viewModelScope.launch {
            val trimmedEmail = email?.trim()?.takeIf { it.isNotBlank() }
            val trimmedCode = code?.trim()?.takeIf { it.isNotBlank() }
            val trimmedToken = token?.trim()?.takeIf { it.isNotBlank() }

            if (trimmedEmail == null && trimmedToken == null) {
                verifyEmailState = RegisterActionUiState.Error("Email or token is required")
                return@launch
            }

            verifyEmailState = RegisterActionUiState.Loading
            verificationResponse = null

            val request = VerifyEmailRequest(
                email = trimmedEmail,
                code = trimmedCode,
                token = trimmedToken
            )

            val result = authRepository.verifyEmail(request)

            result
                .onSuccess { response ->
                    verificationResponse = response
                    verifyEmailState = RegisterActionUiState.Success(response.message ?: response.detail)
                }
                .onFailure { exception ->
                    verifyEmailState = RegisterActionUiState.Error(
                        exception.message ?: "Email verification failed"
                    )
                }
        }
    }

    fun resendVerificationEmail(email: String) {
        viewModelScope.launch {
            val trimmedEmail = email.trim()

            if (trimmedEmail.isBlank()) {
                resendVerificationEmailState = RegisterActionUiState.Error("Email is required")
                return@launch
            }

            resendVerificationEmailState = RegisterActionUiState.Loading
            resendVerificationResponse = null

            val result = authRepository.resendVerificationEmail(trimmedEmail)

            result
                .onSuccess { response ->
                    resendVerificationResponse = response
                    resendVerificationEmailState = RegisterActionUiState.Success(
                        response.message ?: response.detail
                    )
                }
                .onFailure { exception ->
                    resendVerificationEmailState = RegisterActionUiState.Error(
                        exception.message ?: "Failed to resend verification email"
                    )
                }
        }
    }

    fun resetRegisterState() {
        _registerState.value = RegisterActionUiState.Idle
        registerResponse = null
    }

    fun resetVerifyEmailState() {
        verifyEmailState = RegisterActionUiState.Idle
        verificationResponse = null
    }

    fun resetResendVerificationEmailState() {
        resendVerificationEmailState = RegisterActionUiState.Idle
        resendVerificationResponse = null
    }
}
