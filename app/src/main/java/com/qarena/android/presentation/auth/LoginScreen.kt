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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.presentation.common.BrandLogo

@Composable
fun LoginScreen(
    onProfileCompleteLogin: () -> Unit,
    onProfileIncompleteLogin: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    LaunchedEffect(loginViewModel.loginRouteState) {
        when (loginViewModel.loginRouteState) {
            LoginRouteState.SuccessProfileComplete -> {
                loginViewModel.resetLoginRouteState()
                onProfileCompleteLogin()
            }
            LoginRouteState.SuccessProfileIncomplete -> {
                loginViewModel.resetLoginRouteState()
                onProfileIncompleteLogin()
            }
            else -> Unit
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
                text = "Welcome Back",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = loginViewModel.email,
                onValueChange = { loginViewModel.onEmailChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Email")
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = loginViewModel.password,
                onValueChange = { loginViewModel.onPasswordChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Password")
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { loginViewModel.login() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loginViewModel.isLoading
            ) {
                Text(
                    text = if (loginViewModel.isLoading) {
                        "Logging in..."
                    } else {
                        "Login"
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onForgotPasswordClick) {
                Text(text = "Forgot password?")
            }

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(onClick = onRegisterClick) {
                Text(text = "Create an account")
            }

            loginViewModel.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
        }
    }
}
