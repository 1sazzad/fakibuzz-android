package com.qarena.android.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.core.analytics.AnalyticsTracker
import com.qarena.android.data.remote.dto.UserResponse
import com.qarena.android.model.Department
import com.qarena.android.model.University
import com.qarena.android.model.displayLabel
import com.qarena.android.util.AcademicProfile

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel()
) {
    val userState = profileViewModel.userState
    val universitiesState = profileViewModel.universitiesState
    val departmentsState = profileViewModel.departmentsState
    val updateState = profileViewModel.updateState

    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreen(
            screenName = "Profile",
            path = "/android/profile"
        )
        profileViewModel.loadProfile()
        profileViewModel.loadUniversities()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Profile",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            when (userState) {
                ProfileUserUiState.Idle,
                ProfileUserUiState.Loading -> {
                    Text(text = "Loading profile...")
                }

                is ProfileUserUiState.Error -> {
                    Text(
                        text = userState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is ProfileUserUiState.Success -> {
                    ProfileDetails(user = userState.user)

                    Spacer(modifier = Modifier.height(24.dp))

                    AcademicLevelDropdown(
                        selectedAcademicLevel = profileViewModel.selectedAcademicLevel,
                        onSelected = { academicLevel ->
                            profileViewModel.selectAcademicLevel(academicLevel)
                            if (AcademicProfile.isUniversityScoped(academicLevel)) {
                                profileViewModel.loadUniversities()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (AcademicProfile.isUniversityScoped(profileViewModel.selectedAcademicLevel)) {
                        Text(
                            text = "University",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        UniversitySelector(
                            universitiesState = universitiesState,
                            selectedUniversityId = profileViewModel.selectedUniversityId,
                            onUniversitySelected = { university ->
                                profileViewModel.selectUniversity(university)
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Department",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        DepartmentSelector(
                            departmentsState = departmentsState,
                            selectedDepartmentId = profileViewModel.selectedDepartmentId,
                            onDepartmentSelected = { department ->
                                profileViewModel.selectDepartment(department)
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        AcademicTextField(
                            value = profileViewModel.selectedProgram,
                            onValueChange = { profileViewModel.setProgram(it) },
                            label = "Program / degree"
                        )

                        AcademicTextField(
                            value = profileViewModel.selectedBatchSession,
                            onValueChange = { profileViewModel.setBatchSession(it) },
                            label = "Batch session"
                        )
                    } else {
                        DropdownField(
                            label = "Curriculum",
                            selectedText = AcademicProfile.curriculumLabel(profileViewModel.selectedCurriculum),
                            options = AcademicProfile.syllabusCurriculumOptions(),
                            optionLabel = { AcademicProfile.curriculumLabel(it) },
                            onSelected = { profileViewModel.setCurriculum(it) }
                        )

                        DropdownField(
                            label = "Group",
                            selectedText = AcademicProfile.studentStreamGroupLabel(profileViewModel.selectedStreamGroup),
                            options = AcademicProfile.studentStreamGroupOptions(),
                            optionLabel = { AcademicProfile.studentStreamGroupLabel(it) },
                            onSelected = { profileViewModel.setStreamGroup(it) }
                        )

                        DropdownField(
                            label = "Class level",
                            selectedText = AcademicProfile.classLevelLabel(profileViewModel.selectedClassLevel),
                            options = AcademicProfile.classLevelOptions(profileViewModel.selectedAcademicLevel),
                            optionLabel = { AcademicProfile.classLevelLabel(it) },
                            onSelected = { profileViewModel.setClassLevel(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { profileViewModel.saveProfile() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = updateState !is ProfileUpdateUiState.Loading
                    ) {
                        Text(
                            text = if (updateState is ProfileUpdateUiState.Loading) {
                                "Saving..."
                            } else {
                                "Save Profile"
                            }
                        )
                    }

                    ProfileUpdateMessage(updateState = updateState)
                }
            }
        }
    }
}

@Composable
private fun ProfileDetails(
    user: UserResponse
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProfileLine(label = "Full name", value = user.fullName ?: "Not set")
            ProfileLine(label = "Email", value = user.email ?: "Not available")
            ProfileLine(label = "Role", value = user.role ?: "Not available")
            ProfileLine(label = "User ID", value = user.id?.toString() ?: "Not available")
            ProfileLine(
                label = "Email verified",
                value = when (user.isEmailVerified) {
                    true -> "Yes"
                    false -> "No"
                    null -> "Not available"
                }
            )
            ProfileLine(
                label = "University ID",
                value = user.universityId?.toString() ?: "Not set"
            )
            ProfileLine(
                label = "Department ID",
                value = user.departmentId?.toString() ?: "Not set"
            )
        }
    }
}

@Composable
private fun ProfileLine(
    label: String,
    value: String
) {
    Text(
        text = "$label: $value",
        fontSize = 14.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AcademicLevelDropdown(
    selectedAcademicLevel: String,
    onSelected: (String) -> Unit
) {
    DropdownField(
        label = "Academic Level",
        selectedText = AcademicProfile.academicLevelLabel(selectedAcademicLevel),
        options = AcademicProfile.availableAcademicLevels(),
        optionLabel = { AcademicProfile.academicLevelLabel(it) },
        onSelected = onSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    selectedText: String,
    options: List<String>,
    optionLabel: (String) -> String,
    onSelected: (String) -> Unit
) {
    var expanded by androidx.compose.runtime.remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            label = { Text(text = label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AcademicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        label = { Text(text = label) },
        singleLine = true
    )
}

@Composable
private fun UniversitySelector(
    universitiesState: UniversitiesUiState,
    selectedUniversityId: Int?,
    onUniversitySelected: (University) -> Unit
) {
    when (universitiesState) {
        UniversitiesUiState.Idle -> Text(text = "Universities are not loaded")
        UniversitiesUiState.Loading -> Text(text = "Loading universities...")
        is UniversitiesUiState.Error -> Text(
            text = universitiesState.message,
            color = MaterialTheme.colorScheme.error
        )

        is UniversitiesUiState.Success -> {
            if (universitiesState.universities.isEmpty()) {
                Text(text = "No universities found")
            } else {
                universitiesState.universities.forEach { university ->
                    SelectableCard(
                        text = university.displayName(),
                        isSelected = selectedUniversityId == university.id,
                        onClick = { onUniversitySelected(university) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DepartmentSelector(
    departmentsState: DepartmentsUiState,
    selectedDepartmentId: Int?,
    onDepartmentSelected: (Department) -> Unit
) {
    when (departmentsState) {
        DepartmentsUiState.Idle -> Text(text = "Select a university first")
        DepartmentsUiState.Loading -> Text(text = "Loading departments...")
        is DepartmentsUiState.Error -> Text(
            text = departmentsState.message,
            color = MaterialTheme.colorScheme.error
        )

        is DepartmentsUiState.Success -> {
            if (departmentsState.departments.isEmpty()) {
                Text(text = "No departments found")
            } else {
                departmentsState.departments.forEach { department ->
                    SelectableCard(
                        text = department.displayName(),
                        isSelected = selectedDepartmentId == department.id,
                        onClick = { onDepartmentSelected(department) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectableCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ProfileUpdateMessage(
    updateState: ProfileUpdateUiState
) {
    val message = when (updateState) {
        ProfileUpdateUiState.Idle,
        ProfileUpdateUiState.Loading -> null
        is ProfileUpdateUiState.Error -> updateState.message
        is ProfileUpdateUiState.Success -> "Profile saved"
    }

    message?.let {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = it,
            color = if (updateState is ProfileUpdateUiState.Error) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            fontSize = 14.sp
        )
    }
}

private fun University.displayName(): String = displayLabel()

private fun Department.displayName(): String = displayLabel()
