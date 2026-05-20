package com.qarena.android.presentation.subjects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SubjectsScreen(
    onSubjectClick: (String) -> Unit,
    subjectsViewModel: SubjectsViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        subjectsViewModel.loadSubjects()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Subjects",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (subjectsViewModel.isLoading) {
                Text(text = "Loading subjects...")
            } else if (subjectsViewModel.errorMessage != null) {
                Text(
                    text = subjectsViewModel.errorMessage ?: "Failed to load subjects",
                    color = MaterialTheme.colorScheme.error
                )
            } else if (subjectsViewModel.subjects.isEmpty()) {
                Text(text = "No subjects found")
            } else {
                LazyColumn {
                    items(subjectsViewModel.subjects) { subject ->
                        val subjectCode = subject.subjectCode

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    enabled = !subjectCode.isNullOrBlank()
                                ) {
                                    if (!subjectCode.isNullOrBlank()) {
                                        onSubjectClick(subjectCode)
                                    }
                                }
                                .padding(bottom = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = subject.subjectName ?: "Unnamed subject",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = subject.subjectCode ?: "No code",
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = subject.status ?: "Status unavailable",
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
