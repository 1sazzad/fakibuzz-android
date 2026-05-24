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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.model.Department
import com.qarena.android.model.University
import com.qarena.android.model.displayLabel
import com.qarena.android.util.AcademicProfile
import com.qarena.android.util.RegisterFormInput

@Composable
fun ProfileSetupScreen(
    onProfileSaved: () -> Unit,
    profileSetupViewModel: ProfileSetupViewModel = viewModel()
) {
    var academicLevel by remember { mutableStateOf(AcademicProfile.ACADEMIC_LEVEL_UNIVERSITY) }
    var curriculum by remember { mutableStateOf(AcademicProfile.defaultSyllabusCurriculum()) }
    var streamGroup by remember { mutableStateOf(AcademicProfile.STREAM_GROUP_SCIENCE) }
    var program by remember { mutableStateOf("") }
    var batchSession by remember { mutableStateOf("") }
    var selectedUniversity by remember { mutableStateOf<University?>(null) }
    var selectedDepartment by remember { mutableStateOf<Department?>(null) }
    var localErrorMessage by remember { mutableStateOf<String?>(null) }

    val universitiesState = profileSetupViewModel.universitiesState
    val departmentsState = profileSetupViewModel.departmentsState
    val profileSaveState = profileSetupViewModel.profileSaveState

    LaunchedEffect(academicLevel) {
        if (AcademicProfile.isUniversityScoped(academicLevel) && universitiesState is UniversitiesUiState.Idle) {
            profileSetupViewModel.loadUniversities()
        }
    }

    LaunchedEffect(profileSaveState) {
        if (profileSaveState is ProfileSaveUiState.Success) {
            onProfileSaved()
        }
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
                text = "Profile Setup",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose your academic profile",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            AcademicLevelDropdown(
                selectedAcademicLevel = academicLevel,
                onSelected = { level ->
                    academicLevel = level
                    curriculum = AcademicProfile.defaultSyllabusCurriculum()
                    streamGroup = AcademicProfile.STREAM_GROUP_SCIENCE
                    program = if (AcademicProfile.isUniversityScoped(level)) program else ""
                    batchSession = if (AcademicProfile.isUniversityScoped(level)) batchSession else ""
                    selectedUniversity = null
                    selectedDepartment = null
                    localErrorMessage = null
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (AcademicProfile.isUniversityScoped(academicLevel)) {
                Text(text = "University", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                UniversitySelector(
                    universitiesState = universitiesState,
                    selectedUniversity = selectedUniversity,
                    onUniversitySelected = { university ->
                        selectedUniversity = university
                        selectedDepartment = null
                        localErrorMessage = null
                        profileSetupViewModel.loadDepartments(university.id)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Department", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                DepartmentSelector(
                    departmentsState = departmentsState,
                    selectedDepartment = selectedDepartment,
                    onDepartmentSelected = { department ->
                        selectedDepartment = department
                        localErrorMessage = null
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                AcademicTextField(
                    value = program,
                    onValueChange = {
                        program = it
                        localErrorMessage = null
                    },
                    label = "Program / degree"
                )

                AcademicTextField(
                    value = batchSession,
                    onValueChange = {
                        batchSession = it
                        localErrorMessage = null
                    },
                    label = "Batch session"
                )
            } else {
                DropdownField(
                    label = "Curriculum",
                    selectedText = AcademicProfile.curriculumLabel(curriculum),
                    options = AcademicProfile.syllabusCurriculumOptions(),
                    optionLabel = { AcademicProfile.curriculumLabel(it) },
                    onSelected = {
                        curriculum = it
                        localErrorMessage = null
                    }
                )

                DropdownField(
                    label = "Group",
                    selectedText = AcademicProfile.studentStreamGroupLabel(streamGroup),
                    options = AcademicProfile.studentStreamGroupOptions(),
                    optionLabel = { AcademicProfile.studentStreamGroupLabel(it) },
                    onSelected = {
                        streamGroup = it
                        localErrorMessage = null
                    }
                )

            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val universityId = selectedUniversity?.id
                    val departmentId = selectedDepartment?.id

                    localErrorMessage = AcademicProfile.validateProfile(
                        AcademicProfile.toAcademicProfileInput(
                            academicLevel = academicLevel,
                            universityId = universityId,
                            departmentId = departmentId,
                            curriculum = curriculum,
                            streamGroup = streamGroup,
                            classLevel = null,
                            program = program,
                            batchSession = batchSession
                        )
                    )

                    if (localErrorMessage == null) {
                        profileSetupViewModel.updateProfile(
                            academicLevel = academicLevel,
                            universityId = universityId,
                            departmentId = departmentId,
                            curriculum = curriculum,
                            streamGroup = streamGroup,
                            classLevel = null,
                            program = program,
                            batchSession = batchSession
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = profileSaveState !is ProfileSaveUiState.Loading
            ) {
                Text(
                    text = if (profileSaveState is ProfileSaveUiState.Loading) {
                        "Saving..."
                    } else {
                        "Save"
                    }
                )
            }

            ProfileSetupMessage(
                localErrorMessage = localErrorMessage,
                profileSaveState = profileSaveState
            )
        }
    }
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
    var expanded by remember { mutableStateOf(false) }

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
    selectedUniversity: University?,
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
                        isSelected = selectedUniversity?.id == university.id,
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
    selectedDepartment: Department?,
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
                        isSelected = selectedDepartment?.id == department.id,
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
private fun ProfileSetupMessage(
    localErrorMessage: String?,
    profileSaveState: ProfileSaveUiState
) {
    val message = when {
        localErrorMessage != null -> localErrorMessage
        profileSaveState is ProfileSaveUiState.Error -> profileSaveState.message
        profileSaveState is ProfileSaveUiState.Success -> "Profile saved"
        else -> null
    }

    val isError = localErrorMessage != null || profileSaveState is ProfileSaveUiState.Error

    message?.let {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = it,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            fontSize = 14.sp
        )
    }
}

private fun University.displayName(): String = displayLabel()

private fun Department.displayName(): String = displayLabel()
