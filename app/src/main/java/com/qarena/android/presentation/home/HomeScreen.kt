package com.qarena.android.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qarena.android.core.analytics.AnalyticsTracker
import com.qarena.android.core.session.SessionManager
import com.qarena.android.presentation.common.BrandLogo
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onViewSubjectsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: suspend () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val userEmail = SessionManager.userEmail ?: "Guest user"
    val userRole = SessionManager.userRole ?: "Role unavailable"

    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreen(
            screenName = "Home",
            path = "/android/home"
        )
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
                text = "Welcome to Q Arena",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = userEmail,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userRole,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your smart exam preparation assistant",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onViewSubjectsClick
            ) {
                Text(text = "View Subjects")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSearchClick
            ) {
                Text(text = "Search")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onFeedbackClick
            ) {
                Text(text = "Feedback")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onProfileClick
            ) {
                Text(text = "Profile")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        onLogoutClick()
                    }
                }
            ) {
                Text(text = "Logout")
            }
        }
    }
}
