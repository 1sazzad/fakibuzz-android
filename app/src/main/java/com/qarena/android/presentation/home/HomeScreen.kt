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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qarena.android.core.session.SessionManager

@Composable
fun HomeScreen(
    onViewSubjectsClick: () -> Unit
) {
    val userEmail = SessionManager.userEmail ?: "Guest user"
    val userRole = SessionManager.userRole ?: "Role unavailable"

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
        }
    }
}
