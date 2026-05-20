package com.qarena.android.presentation.questions

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
fun QuestionsScreen(
    subjectCode: String,
    questionsViewModel: QuestionsViewModel = viewModel()
) {
    LaunchedEffect(subjectCode) {
        questionsViewModel.loadQuestions(subjectCode)
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
                text = "Questions",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Subject: $subjectCode",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (questionsViewModel.isLoading) {
                Text(text = "Loading questions...")
            } else if (questionsViewModel.errorMessage != null) {
                Text(
                    text = questionsViewModel.errorMessage ?: "Failed to load questions",
                    color = MaterialTheme.colorScheme.error
                )
            } else if (questionsViewModel.questions.isEmpty()) {
                Text(text = "No questions found")
            } else {
                LazyColumn {
                    items(questionsViewModel.questions) { question ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = question.questionNo ?: "Q",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = question.questionText ?: "No question text",
                                    fontSize = 16.sp
                                )

                                question.marks?.let { marks ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Marks: $marks",
                                        fontSize = 14.sp
                                    )
                                }

                                question.topic?.let { topic ->
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Topic: $topic",
                                        fontSize = 14.sp
                                    )
                                }

                                question.examYear?.let { examYear ->
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Exam year: $examYear",
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
}
