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
import com.qarena.android.util.AcademicProfileInput
import kotlinx.coroutines.launch

sealed interface ProfileUserUiState {
    data object Idle : ProfileUserUiState
    data object Loading : ProfileUserUiState
    data class Success(val user: UserResponse) : ProfileUserUiState
    data class Error(val message: String) : ProfileUserUiState
}

sealed interface ProfileUpdateUiState {
    data object Idle : ProfileUpdateUiState
    data object Loading : ProfileUpdateUiState
    data class Success(val user: UserResponse) : ProfileUpdateUiState
    data class Error(val message: String) : ProfileUpdateUiState
}

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val publicRepository: PublicRepository = PublicRepository()
) : ViewModel() {

    var userState by mutableStateOf<ProfileUserUiState>(ProfileUserUiState.Idle)
        private set

    var universitiesState by mutableStateOf<UniversitiesUiState>(UniversitiesUiState.Idle)
        private set

    var departmentsState by mutableStateOf<DepartmentsUiState>(DepartmentsUiState.Idle)
        private set

    var updateState by mutableStateOf<ProfileUpdateUiState>(ProfileUpdateUiState.Idle)
        private set

    var selectedAcademicLevel by mutableStateOf("")
        private set

    var selectedUniversityId by mutableStateOf<Int?>(null)
        private set

    var selectedDepartmentId by mutableStateOf<Int?>(null)
        private set

    var selectedCurriculum by mutableStateOf("")
        private set

    var selectedStreamGroup by mutableStateOf("")
        private set

    var selectedClassLevel by mutableStateOf("")
        private set

    var selectedProgram by mutableStateOf("")
        private set

    var selectedBatchSession by mutableStateOf("")
        private set

    fun loadProfile() {
        viewModelScope.launch {
            val token = SessionManager.accessToken

            if (token.isNullOrBlank()) {
                userState = ProfileUserUiState.Error("Not logged in")
                return@launch
            }

            userState = ProfileUserUiState.Loading
            val result = authRepository.getCurrentUser(token)

            result
                .onSuccess { user ->
                    val academicLevel = AcademicProfile.resolveAcademicLevel(user)
                    selectedAcademicLevel = academicLevel
                    selectedUniversityId = user.universityId
                    selectedDepartmentId = user.departmentId
                    selectedCurriculum = user.curriculum.orEmpty()
                    selectedStreamGroup = AcademicProfile.normalizeStudentStreamGroup(user.streamGroup).orEmpty()
                    selectedClassLevel = user.classLevel.orEmpty()
                    selectedProgram = user.program.orEmpty()
                    selectedBatchSession = user.batchSession.orEmpty()
                    SessionManager.saveSession(
                        token = token,
                        email = user.email,
                        role = user.role,
                        userId = user.id
                    )
                    userState = ProfileUserUiState.Success(user)

                    if (AcademicProfile.shouldLoadDepartments(user)) {
                        user.universityId?.let { universityId ->
                            loadDepartments(universityId, keepDepartment = true)
                        }
                    }
                }
                .onFailure { exception ->
                    userState = ProfileUserUiState.Error(
                        exception.message ?: "Failed to load profile"
                    )
                }
        }
    }

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

    fun selectUniversity(university: University) {
        val universityId = university.id

        selectedAcademicLevel = AcademicProfile.ACADEMIC_LEVEL_UNIVERSITY
        selectedUniversityId = universityId
        selectedDepartmentId = null
        selectedCurriculum = AcademicProfile.CURRICULUM_UNIVERSITY_SPECIFIC
        selectedStreamGroup = ""
        selectedClassLevel = ""
        selectedProgram = selectedProgram.trim()
        selectedBatchSession = selectedBatchSession.trim()
        updateState = ProfileUpdateUiState.Idle
        loadDepartments(universityId, keepDepartment = false)
    }

    fun selectDepartment(department: Department) {
        selectedDepartmentId = department.id
        updateState = ProfileUpdateUiState.Idle
    }

    fun selectAcademicLevel(academicLevel: String) {
        val switchedProfile = AcademicProfile.switchAcademicLevel(
            profile = AcademicProfileInput(
                academicLevel = selectedAcademicLevel,
                universityId = selectedUniversityId,
                departmentId = selectedDepartmentId,
                curriculum = selectedCurriculum,
                streamGroup = selectedStreamGroup,
                classLevel = selectedClassLevel,
                program = selectedProgram,
                batchSession = selectedBatchSession
            ),
            academicLevel = academicLevel
        )

        selectedAcademicLevel = switchedProfile.academicLevel
        selectedUniversityId = switchedProfile.universityId
        selectedDepartmentId = switchedProfile.departmentId
        selectedCurriculum = switchedProfile.curriculum.orEmpty()
        selectedStreamGroup = switchedProfile.streamGroup.orEmpty()
        selectedClassLevel = switchedProfile.classLevel.orEmpty()
        selectedProgram = switchedProfile.program.orEmpty()
        selectedBatchSession = switchedProfile.batchSession.orEmpty()
        updateState = ProfileUpdateUiState.Idle

        if (AcademicProfile.isUniversityScoped(selectedAcademicLevel)) {
            departmentsState = DepartmentsUiState.Idle
        } else if (AcademicProfile.isSyllabusScoped(selectedAcademicLevel)) {
            universitiesState = UniversitiesUiState.Idle
            departmentsState = DepartmentsUiState.Idle
        }
    }

    fun setCurriculum(curriculum: String) {
        selectedCurriculum = curriculum.trim()
        updateState = ProfileUpdateUiState.Idle
    }

    fun setStreamGroup(streamGroup: String) {
        selectedStreamGroup = AcademicProfile.normalizeStudentStreamGroup(streamGroup).orEmpty()
        updateState = ProfileUpdateUiState.Idle
    }

    fun setClassLevel(classLevel: String) {
        selectedClassLevel = classLevel.trim()
        updateState = ProfileUpdateUiState.Idle
    }

    fun setProgram(program: String) {
        selectedProgram = program
        updateState = ProfileUpdateUiState.Idle
    }

    fun setBatchSession(batchSession: String) {
        selectedBatchSession = batchSession
        updateState = ProfileUpdateUiState.Idle
    }

    fun saveProfile() {
        viewModelScope.launch {
            val token = SessionManager.accessToken
            val academicLevel = selectedAcademicLevel
            val universityId = selectedUniversityId
            val departmentId = selectedDepartmentId
            val currentUser = (userState as? ProfileUserUiState.Success)?.user

            if (token.isNullOrBlank()) {
                updateState = ProfileUpdateUiState.Error("Not logged in")
                return@launch
            }

            val profileInput = AcademicProfile.toAcademicProfileInput(
                academicLevel = academicLevel,
                universityId = universityId,
                departmentId = departmentId,
                curriculum = selectedCurriculum,
                streamGroup = selectedStreamGroup,
                classLevel = selectedClassLevel,
                program = selectedProgram,
                batchSession = selectedBatchSession
            )

            val validationError = AcademicProfile.validateProfile(profileInput)

            if (validationError != null) {
                updateState = ProfileUpdateUiState.Error(validationError)
                return@launch
            }

            updateState = ProfileUpdateUiState.Loading

            val result = authRepository.updateProfile(
                token = token,
                request = AcademicProfile.buildProfileUpdateRequest(
                    fullName = currentUser?.fullName,
                    academicLevel = academicLevel,
                    universityId = universityId,
                    departmentId = departmentId,
                    curriculum = selectedCurriculum,
                    streamGroup = selectedStreamGroup,
                    classLevel = selectedClassLevel,
                    program = selectedProgram,
                    batchSession = selectedBatchSession
                )
            )

            result
                .onSuccess { user ->
                    SessionManager.saveSession(
                        token = token,
                        email = user.email,
                        role = user.role,
                        userId = user.id
                    )
                    selectedAcademicLevel = AcademicProfile.resolveAcademicLevel(user)
                    selectedUniversityId = user.universityId ?: universityId
                    selectedDepartmentId = user.departmentId ?: departmentId
                    selectedCurriculum = user.curriculum.orEmpty()
                    selectedStreamGroup = AcademicProfile.normalizeStudentStreamGroup(user.streamGroup).orEmpty()
                    selectedClassLevel = user.classLevel.orEmpty()
                    selectedProgram = user.program.orEmpty()
                    selectedBatchSession = user.batchSession.orEmpty()
                    userState = ProfileUserUiState.Success(user)
                    updateState = ProfileUpdateUiState.Success(user)
                }
                .onFailure { exception ->
                    updateState = ProfileUpdateUiState.Error(
                        exception.message ?: "Failed to save profile"
                    )
                }
        }
    }

    fun resetUpdateState() {
        updateState = ProfileUpdateUiState.Idle
    }

    private fun loadDepartments(
        universityId: Int,
        keepDepartment: Boolean
    ) {
        viewModelScope.launch {
            departmentsState = DepartmentsUiState.Loading

            val result = publicRepository.getDepartments(universityId)

            result
                .onSuccess { response ->
                    if (!keepDepartment) {
                        selectedDepartmentId = null
                    }
                    departmentsState = DepartmentsUiState.Success(response.departments)
                }
                .onFailure { exception ->
                    departmentsState = DepartmentsUiState.Error(
                        ApiErrorParser.messageForThrowable(exception)
                    )
                }
        }
    }

}
