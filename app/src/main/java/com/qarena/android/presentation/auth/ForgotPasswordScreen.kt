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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.presentation.common.BrandLogo

@Composable
fun ForgotPasswordScreen(
    onBackToLoginClick: () -> Unit,
    onResetPasswordClick: () -> Unit,
    forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()
) {
    val forgotPasswordState = forgotPasswordViewModel.forgotPasswordState

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
                text = "Forgot Password",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = forgotPasswordViewModel.email,
                onValueChange = { forgotPasswordViewModel.onEmailChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Email") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { forgotPasswordViewModel.forgotPassword() },
                modifier = Modifier.fillMaxWidth(),
                enabled = forgotPasswordState !is ForgotPasswordUiState.Loading
            ) {
                Text(
                    text = if (forgotPasswordState is ForgotPasswordUiState.Loading) {
                        "Sending..."
                    } else {
                        "Send reset instructions"
                    }
                )
            }

            ForgotPasswordMessage(forgotPasswordState = forgotPasswordState)

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onResetPasswordClick) {
                Text(text = "I have a reset token")
            }

            TextButton(onClick = onBackToLoginClick) {
                Text(text = "Back to login")
            }
        }
    }
}

@Composable
private fun ForgotPasswordMessage(
    forgotPasswordState: ForgotPasswordUiState
) {
    val message = when (forgotPasswordState) {
        ForgotPasswordUiState.Idle,
        ForgotPasswordUiState.Loading -> null
        is ForgotPasswordUiState.Error -> forgotPasswordState.message
        is ForgotPasswordUiState.Success -> {
            forgotPasswordState.response.message
                ?: forgotPasswordState.response.detail
                ?: "Reset instructions sent"
        }
    }

    message?.let {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = it,
            color = if (forgotPasswordState is ForgotPasswordUiState.Error) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            fontSize = 14.sp
        )
    }
}
