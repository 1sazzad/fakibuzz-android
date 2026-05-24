package com.qarena.android.presentation.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.core.session.SessionManager
import com.qarena.android.data.remote.ApiErrorParser
import com.qarena.android.data.remote.dto.UserResponse
import com.qarena.android.data.repository.AuthRepository
import com.qarena.android.data.repository.PublicRepository
import com.qarena.android.model.Department
import com.qarena.android.model.University
import com.qarena.android.util.AcademicProfile
import kotlinx.coroutines.launch

sealed interface UniversitiesUiState {
    data object Idle : UniversitiesUiState
    data object Loading : UniversitiesUiState
    data class Success(val universities: List<University>) : UniversitiesUiState
    data class Error(val message: String) : UniversitiesUiState
}

sealed interface DepartmentsUiState {
    data object Idle : DepartmentsUiState
    data object Loading : DepartmentsUiState
    data class Success(val departments: List<Department>) : DepartmentsUiState
    data class Error(val message: String) : DepartmentsUiState
}

sealed interface ProfileSaveUiState {
    data object Idle : ProfileSaveUiState
    data object Loading : ProfileSaveUiState
    data class Success(val user: UserResponse) : ProfileSaveUiState
    data class Error(val message: String) : ProfileSaveUiState
}

class ProfileSetupViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val publicRepository: PublicRepository = PublicRepository()
) : ViewModel() {

    var universitiesState by mutableStateOf<UniversitiesUiState>(UniversitiesUiState.Idle)
        private set

    var departmentsState by mutableStateOf<DepartmentsUiState>(DepartmentsUiState.Idle)
        private set

    var profileSaveState by mutableStateOf<ProfileSaveUiState>(ProfileSaveUiState.Idle)
        private set

    var selectedUniversityId by mutableStateOf<Int?>(null)
        private set

    var selectedDepartmentId by mutableStateOf<Int?>(null)
        private set

    fun loadUniversities() {
        viewModelScope.launch {
            universitiesState = UniversitiesUiState.Loading

            val result = publicRepository.getUniversities()

            result
                .onSuccess { response ->
                    universitiesState = UniversitiesUiState.Success(response.universities)
                }
                .onFailure { exception ->
                    universitiesState = UniversitiesUiState.Error(
                        ApiErrorParser.messageForThrowable(exception)
                    )
                }
        }
    }

    fun loadDepartments(universityId: Int) {
        viewModelScope.launch {
            if (universityId <= 0) {
                departmentsState = DepartmentsUiState.Error("University is required")
                return@launch
            }

            selectedUniversityId = universityId
            selectedDepartmentId = null
            departmentsState = DepartmentsUiState.Loading

            val result = publicRepository.getDepartments(universityId)

            result
                .onSuccess { response ->
                    departmentsState = DepartmentsUiState.Success(response.departments)
                }
                .onFailure { exception ->
                    departmentsState = DepartmentsUiState.Error(
                        ApiErrorParser.messageForThrowable(exception)
                    )
                }
        }
    }

    fun updateProfile(
        academicLevel: String,
        universityId: Int? = null,
        departmentId: Int? = null,
        curriculum: String? = null,
        streamGroup: String? = null,
        classLevel: String? = null,
        program: String? = null,
        batchSession: String? = null,
        fullName: String? = null
    ) {
        viewModelScope.launch {
            val token = SessionManager.accessToken

            if (token.isNullOrBlank()) {
                profileSaveState = ProfileSaveUiState.Error("Not logged in")
                return@launch
            }

            val request = AcademicProfile.buildProfileUpdateRequest(
                fullName = fullName,
                academicLevel = academicLevel,
                universityId = universityId,
                departmentId = departmentId,
                curriculum = curriculum,
                streamGroup = streamGroup,
                classLevel = classLevel,
                program = program,
                batchSession = batchSession
            )

            val validationError = AcademicProfile.validateProfile(
                AcademicProfile.toAcademicProfileInput(
                    academicLevel = academicLevel,
                    universityId = universityId,
                    departmentId = departmentId,
                    curriculum = curriculum,
                    streamGroup = streamGroup,
                    classLevel = classLevel,
                    program = program,
                    batchSession = batchSession
                )
            )

            if (validationError != null) {
                profileSaveState = ProfileSaveUiState.Error(validationError)
                return@launch
            }

            profileSaveState = ProfileSaveUiState.Loading

            val result = authRepository.updateProfile(
                token = token,
                request = request
            )

            result
                .onSuccess { user ->
                    val token = SessionManager.accessToken

                    if (!token.isNullOrBlank()) {
                        SessionManager.saveSession(
                            token = token,
                            email = user.email,
                            role = user.role,
                            userId = user.id,
                            academicLevel = AcademicProfile.resolveAcademicLevel(user),
                            universityId = user.universityId,
                            departmentId = user.departmentId,
                            curriculum = user.curriculum,
                            streamGroup = user.streamGroup
                        )
                    }

                    profileSaveState = ProfileSaveUiState.Success(user)
                }
                .onFailure { exception ->
                    profileSaveState = ProfileSaveUiState.Error(
                        exception.message ?: "Failed to save profile"
                    )
                }
        }
    }

    fun resetProfileSaveState() {
        profileSaveState = ProfileSaveUiState.Idle
    }
}
