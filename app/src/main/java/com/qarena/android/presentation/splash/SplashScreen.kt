package com.qarena.android.presentation.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.presentation.common.BrandLogo

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToProfileSetup: () -> Unit,
    onNavigateToHome: () -> Unit,
    splashViewModel: SplashViewModel = viewModel()
) {
    val startupRouteState = splashViewModel.startupRouteState

    LaunchedEffect(Unit) {
        splashViewModel.checkStartupRoute()
    }

    LaunchedEffect(startupRouteState) {
        when (startupRouteState) {
            StartupRouteState.Checking -> Unit
            StartupRouteState.Login -> onNavigateToLogin()
            StartupRouteState.ProfileSetup -> onNavigateToProfileSetup()
            StartupRouteState.Home -> onNavigateToHome()
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
                text = "Q Arena",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Revise smarter. Prepare faster.",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Checking your session...",
                fontSize = 14.sp
            )
        }
    }
}
