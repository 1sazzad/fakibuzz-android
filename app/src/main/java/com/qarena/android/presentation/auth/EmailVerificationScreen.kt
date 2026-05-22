package com.qarena.android.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.presentation.common.BrandLogo

@Composable
fun EmailVerificationScreen(
    onVerificationSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    registerViewModel: RegisterViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var verificationTokenOrCode by remember { mutableStateOf("") }
    var localErrorMessage by remember { mutableStateOf<String?>(null) }

    val verifyState = registerViewModel.verifyEmailState
    val resendState = registerViewModel.resendVerificationEmailState

    LaunchedEffect(verifyState) {
        if (verifyState is RegisterActionUiState.Success) {
            onVerificationSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandLogo()

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Verify Email",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    localErrorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Email") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = verificationTokenOrCode,
                onValueChange = {
                    verificationTokenOrCode = it
                    localErrorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Verification token or code") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedTokenOrCode = verificationTokenOrCode.trim()

                    localErrorMessage = when {
                        trimmedEmail.isBlank() -> "Email is required"
                        trimmedTokenOrCode.isBlank() -> "Verification token or code is required"
                        else -> null
                    }

                    if (localErrorMessage == null) {
                        registerViewModel.verifyEmail(
                            email = trimmedEmail,
                            code = trimmedTokenOrCode,
                            token = trimmedTokenOrCode
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = verifyState !is RegisterActionUiState.Loading
            ) {
                Text(
                    text = if (verifyState is RegisterActionUiState.Loading) {
                        "Verifying..."
                    } else {
                        "Verify"
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    val trimmedEmail = email.trim()

                    localErrorMessage = if (trimmedEmail.isBlank()) {
                        "Email is required"
                    } else {
                        null
                    }

                    if (localErrorMessage == null) {
                        registerViewModel.resendVerificationEmail(trimmedEmail)
                    }
                },
                enabled = resendState !is RegisterActionUiState.Loading
            ) {
                Text(
                    text = if (resendState is RegisterActionUiState.Loading) {
                        "Resending..."
                    } else {
                        "Resend verification"
                    }
                )
            }

            VerificationMessage(
                localErrorMessage = localErrorMessage,
                verifyState = verifyState,
                resendState = resendState
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onLoginClick) {
                Text(text = "Back to login")
            }
        }
    }
}

@Composable
private fun VerificationMessage(
    localErrorMessage: String?,
    verifyState: RegisterActionUiState,
    resendState: RegisterActionUiState
) {
    val message = when {
        localErrorMessage != null -> localErrorMessage
        verifyState is RegisterActionUiState.Error -> verifyState.message
        resendState is RegisterActionUiState.Error -> resendState.message
        verifyState is RegisterActionUiState.Success -> {
            verifyState.message ?: "Email verified"
        }
        resendState is RegisterActionUiState.Success -> {
            resendState.message ?: "Verification email sent"
        }
        else -> null
    }

    val isError = localErrorMessage != null ||
        verifyState is RegisterActionUiState.Error ||
        resendState is RegisterActionUiState.Error

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
