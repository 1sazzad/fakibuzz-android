package com.qarena.android.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.presentation.common.BrandLogo

@Composable
fun ResetPasswordScreen(
    onBackToLoginClick: () -> Unit,
    resetPasswordViewModel: ResetPasswordViewModel = viewModel()
) {
    val resetPasswordState = resetPasswordViewModel.resetPasswordState

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
                text = "Reset Password",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = resetPasswordViewModel.token,
                onValueChange = { resetPasswordViewModel.onTokenChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Reset token") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = resetPasswordViewModel.newPassword,
                onValueChange = { resetPasswordViewModel.onNewPasswordChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "New password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = resetPasswordViewModel.confirmPassword,
                onValueChange = { resetPasswordViewModel.onConfirmPasswordChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Confirm password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { resetPasswordViewModel.resetPassword() },
                modifier = Modifier.fillMaxWidth(),
                enabled = resetPasswordState !is ResetPasswordUiState.Loading
            ) {
                Text(
                    text = if (resetPasswordState is ResetPasswordUiState.Loading) {
                        "Resetting..."
                    } else {
                        "Reset password"
                    }
                )
            }

            ResetPasswordMessage(resetPasswordState = resetPasswordState)

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onBackToLoginClick) {
                Text(text = "Back to login")
            }
        }
    }
}

@Composable
private fun ResetPasswordMessage(
    resetPasswordState: ResetPasswordUiState
) {
    val message = when (resetPasswordState) {
        ResetPasswordUiState.Idle,
        ResetPasswordUiState.Loading -> null
        is ResetPasswordUiState.Error -> resetPasswordState.message
        is ResetPasswordUiState.Success -> {
            resetPasswordState.response.message
                ?: resetPasswordState.response.detail
                ?: "Password reset successful"
        }
    }

    message?.let {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = it,
            color = if (resetPasswordState is ResetPasswordUiState.Error) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            fontSize = 14.sp
        )
    }
}
