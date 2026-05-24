package com.qarena.android.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.model.Department
import com.qarena.android.model.University
import com.qarena.android.model.displayLabel
import com.qarena.android.util.AcademicProfile
import com.qarena.android.util.RegisterFormInput
import com.qarena.android.presentation.common.BrandLogo

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    registerViewModel: RegisterViewModel = viewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var academicLevel by remember { mutableStateOf(AcademicProfile.ACADEMIC_LEVEL_UNIVERSITY) }
    var curriculum by remember { mutableStateOf(AcademicProfile.defaultSyllabusCurriculum()) }
    var streamGroup by remember { mutableStateOf(AcademicProfile.STREAM_GROUP_SCIENCE) }
    var program by remember { mutableStateOf("") }
    var batchSession by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var selectedUniversity by remember { mutableStateOf<University?>(null) }
    var selectedDepartment by remember { mutableStateOf<Department?>(null) }
    var localErrorMessage by remember { mutableStateOf<String?>(null) }

    val registerState by registerViewModel.registerState.collectAsState()
    val universitiesState by registerViewModel.universitiesState.collectAsState()
    val departmentsState by registerViewModel.departmentsState.collectAsState()
    val isLoading = registerState is RegisterActionUiState.Loading
    val isSuccess = registerState is RegisterActionUiState.Success

    LaunchedEffect(academicLevel) {
        if (AcademicProfile.isUniversityScoped(academicLevel)) {
            if (universitiesState is RegisterLookupUiState.Idle) {
                registerViewModel.loadUniversities()
            }
        } else {
            registerViewModel.clearUniversities()
            registerViewModel.clearDepartments()
            selectedUniversity = null
            selectedDepartment = null
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
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandLogo()

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Create Account",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            AcademicLevelDropdown(
                selectedAcademicLevel = academicLevel,
                enabled = !isLoading,
                onSelected = { level ->
                    academicLevel = level
                    curriculum = AcademicProfile.defaultSyllabusCurriculum()
                    streamGroup = AcademicProfile.STREAM_GROUP_SCIENCE
                    program = if (AcademicProfile.isUniversityScoped(level)) program else ""
                    batchSession = if (AcademicProfile.isUniversityScoped(level)) batchSession else ""
                    localErrorMessage = null
                    registerViewModel.resetRegisterState()
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            RegisterTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    localErrorMessage = null
                    registerViewModel.resetRegisterState()
                },
                label = "Full name",
                enabled = !isLoading
            )

            RegisterTextField(
                value = email,
                onValueChange = {
                    email = it
                    localErrorMessage = null
                    registerViewModel.resetRegisterState()
                },
                label = "Email",
                enabled = !isLoading
            )

            RegisterTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    localErrorMessage = null
                    registerViewModel.resetRegisterState()
                },
                label = "Phone number",
                enabled = !isLoading
            )

            if (AcademicProfile.isUniversityScoped(academicLevel)) {
                InstitutionDropdown(
                    label = "University",
                    selectedText = selectedUniversity.displayNameOrEmpty(),
                    placeholder = "Select university",
                    loadingText = "Loading universities...",
                    emptyText = "No universities found",
                    state = universitiesState,
                    itemText = { it.displayName() },
                    enabled = !isLoading,
                    onSelected = { university ->
                        selectedUniversity = university
                        selectedDepartment = null
                        localErrorMessage = null
                        registerViewModel.resetRegisterState()
                        registerViewModel.loadDepartments(university.id)
                    }
                )

                InstitutionDropdown(
                    label = "Department",
                    selectedText = selectedDepartment.displayNameOrEmpty(),
                    placeholder = "Select department",
                    loadingText = "Loading departments...",
                    emptyText = "No departments found",
                    state = departmentsState,
                    itemText = { it.displayName() },
                    enabled = !isLoading && selectedUniversity != null,
                    onSelected = { department ->
                        selectedDepartment = department
                        localErrorMessage = null
                        registerViewModel.resetRegisterState()
                    }
                )

                RegisterTextField(
                    value = program,
                    onValueChange = {
                        program = it
                        localErrorMessage = null
                    },
                    label = "Program / degree",
                    enabled = !isLoading
                )

                RegisterTextField(
                    value = batchSession,
                    onValueChange = {
                        batchSession = it
                        localErrorMessage = null
                    },
                    label = "Batch session",
                    enabled = !isLoading
                )
            } else {
                DropdownField(
                    label = "Curriculum",
                    selectedText = AcademicProfile.curriculumLabel(curriculum),
                    options = AcademicProfile.syllabusCurriculumOptions(),
                    enabled = !isLoading,
                    optionLabel = { AcademicProfile.curriculumLabel(it) },
                    onSelected = {
                        curriculum = it
                        localErrorMessage = null
                        registerViewModel.resetRegisterState()
                    }
                )

                DropdownField(
                    label = "Group",
                    selectedText = AcademicProfile.studentStreamGroupLabel(streamGroup),
                    options = AcademicProfile.studentStreamGroupOptions(),
                    enabled = !isLoading,
                    optionLabel = { AcademicProfile.studentStreamGroupLabel(it) },
                    onSelected = {
                        streamGroup = it
                        localErrorMessage = null
                        registerViewModel.resetRegisterState()
                    }
                )

            }

            RegisterTextField(
                value = password,
                onValueChange = {
                    password = it
                    localErrorMessage = null
                    registerViewModel.resetRegisterState()
                },
                label = "Password",
                enabled = !isLoading,
                isPassword = true
            )

            RegisterTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    localErrorMessage = null
                    registerViewModel.resetRegisterState()
                },
                label = "Confirm password",
                enabled = !isLoading,
                isPassword = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = {
                        termsAccepted = it
                        localErrorMessage = null
                        registerViewModel.resetRegisterState()
                    },
                    enabled = !isLoading
                )
                Text(text = "I accept the terms")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val universityId = selectedUniversity?.id
                    val departmentId = selectedDepartment?.id
                    localErrorMessage = RegisterInputValidator.validate(
                        RegisterFormInput(
                            fullName = fullName,
                            email = email,
                            phone = phone,
                            password = password,
                            confirmPassword = confirmPassword,
                            academicLevel = academicLevel,
                            universityId = universityId,
                            departmentId = departmentId,
                            curriculum = curriculum,
                            streamGroup = streamGroup,
                            classLevel = null,
                            termsAccepted = termsAccepted
                        )
                    )

                    if (localErrorMessage == null) {
                        registerViewModel.register(
                            fullName = fullName,
                            email = email,
                            phone = phone,
                            password = password,
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
                enabled = !isLoading && !isSuccess
            ) {
                Text(text = if (isLoading) "Creating account..." else "Register")
            }

            RegisterMessage(
                localErrorMessage = localErrorMessage,
                registerState = registerState
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onLoginClick) {
                Text(text = if (isSuccess) "Go to Login" else "Already have account? Login")
            }
        }
    }
}

@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        label = { Text(text = label) },
        singleLine = true,
        enabled = enabled,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    selectedText: String,
    options: List<String>,
    enabled: Boolean,
    optionLabel: (String) -> String = { it },
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
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
            enabled = enabled,
            label = { Text(text = label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = optionLabel(option).ifBlank { option }) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AcademicLevelDropdown(
    selectedAcademicLevel: String,
    enabled: Boolean,
    onSelected: (String) -> Unit
) {
    DropdownField(
        label = "Academic Level",
        selectedText = AcademicProfile.academicLevelLabel(selectedAcademicLevel),
        options = AcademicProfile.availableAcademicLevels(),
        enabled = enabled,
        onSelected = onSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> InstitutionDropdown(
    label: String,
    selectedText: String,
    placeholder: String,
    loadingText: String,
    emptyText: String,
    state: RegisterLookupUiState<T>,
    itemText: (T) -> String,
    enabled: Boolean,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (enabled && state is RegisterLookupUiState.Success) {
                expanded = !expanded
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = selectedText,
            onValueChange = {},
            label = { Text(text = label) },
            placeholder = { Text(text = placeholder) },
            enabled = enabled,
            supportingText = {
                if (state is RegisterLookupUiState.Error) {
                    Text(text = state.message)
                }
            },
            trailingIcon = {
                if (state is RegisterLookupUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(8.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            when (state) {
                RegisterLookupUiState.Idle -> DropdownMenuItem(
                    text = { Text(text = placeholder) },
                    onClick = { expanded = false }
                )
                RegisterLookupUiState.Loading -> DropdownMenuItem(
                    text = { Text(text = loadingText) },
                    onClick = { expanded = false }
                )
                is RegisterLookupUiState.Error -> DropdownMenuItem(
                    text = { Text(text = state.message) },
                    onClick = { expanded = false }
                )
                is RegisterLookupUiState.Success -> {
                    if (state.items.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text(text = emptyText) },
                            onClick = { expanded = false }
                        )
                    } else {
                        state.items.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = itemText(item)) },
                                onClick = {
                                    onSelected(item)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterMessage(
    localErrorMessage: String?,
    registerState: RegisterActionUiState
) {
    val message = when {
        localErrorMessage != null -> localErrorMessage
        registerState is RegisterActionUiState.Error -> registerState.message
        registerState is RegisterActionUiState.Success -> registerState.message
        else -> null
    }

    val isError = localErrorMessage != null || registerState is RegisterActionUiState.Error

    message?.let {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = it,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            fontSize = 14.sp
        )
    }
}

private fun University?.displayNameOrEmpty(): String {
    return this?.displayName().orEmpty()
}

private fun Department?.displayNameOrEmpty(): String {
    return this?.displayName().orEmpty()
}

private fun University.displayName(): String {
    return displayLabel()
}

private fun Department.displayName(): String {
    return displayLabel()
}
