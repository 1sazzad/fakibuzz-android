package com.qarena.android.presentation.subjects

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.data.remote.dto.SubjectOverviewResponse
import com.qarena.android.model.Subject
import com.qarena.android.model.displayLabel
import com.qarena.android.model.displaySubtitle
import com.qarena.android.presentation.common.AnswerPayload
import com.qarena.android.presentation.navigation.SelectedSubjectNavArgs
import com.qarena.android.presentation.subjects.components.NextStepActionCard
import com.qarena.android.presentation.subjects.components.QuestionCard
import com.qarena.android.presentation.subjects.components.QuestionsEmptyStateCard
import com.qarena.android.presentation.subjects.components.QuestionsErrorStateCard
import com.qarena.android.presentation.subjects.components.QuestionsLoadingState
import com.qarena.android.presentation.subjects.components.SubjectPublishedQuestionsHeader
import com.qarena.android.util.PaperTypeLookups

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDiscoveryScreen(
    initialSubjectCode: String? = null,
    onOpenSimilarQuestionsClick: (SelectedSubjectNavArgs) -> Unit,
    onOpenTopicAnalysisClick: (SelectedSubjectNavArgs) -> Unit,
    onOpenPredictionsClick: (SelectedSubjectNavArgs) -> Unit,
    onOpenAnswerBuilderClick: (SelectedSubjectNavArgs) -> Unit,
    onOpenSubjectOverviewClick: ((String) -> Unit)? = null,
    onGetAnswerClick: ((AnswerPayload) -> Unit)? = null,
    subjectDiscoveryViewModel: SubjectDiscoveryViewModel = viewModel()
) {
    val selectedSubject = subjectDiscoveryViewModel.selectedSubject
    val overview = subjectDiscoveryViewModel.selectedSubjectOverview
    val publishedSubjects = subjectDiscoveryViewModel.publishedSubjects
    val selectedPaperType = subjectDiscoveryViewModel.selectedPaperType
    val questions = subjectDiscoveryViewModel.questions
    val questionsLoading = subjectDiscoveryViewModel.isLoadingQuestions
    val subjectsLoading = subjectDiscoveryViewModel.isLoadingSubjects
    val errorMessage = subjectDiscoveryViewModel.errorMessage
    val publishedSubjectsErrorMessage = subjectDiscoveryViewModel.publishedSubjectsErrorMessage

    LaunchedEffect(initialSubjectCode) {
        subjectDiscoveryViewModel.bootstrap(initialSubjectCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Subject Discovery",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Find published data by subject code or name",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DiscoverySearchSection(
                    searchQuery = subjectDiscoveryViewModel.searchQuery,
                    onSearchQueryChange = subjectDiscoveryViewModel::onSearchQueryChange,
                    subjects = publishedSubjects,
                    subjectsLoading = subjectsLoading,
                    publishedSubjectsErrorMessage = publishedSubjectsErrorMessage,
                    onSearchClick = {
                        subjectDiscoveryViewModel.searchPublishedSubjects(subjectDiscoveryViewModel.searchQuery)
                    },
                    onBrowseClick = {
                        subjectDiscoveryViewModel.loadPublishedSubjects()
                    },
                    onSubjectSelected = { subject ->
                        subjectDiscoveryViewModel.selectSubject(subject)
                    }
                )
            }

            if (selectedSubject == null) {
                item {
                    QuestionsEmptyStateCard(
                        title = "Choose a subject",
                        description = "Search by subject code or name, then open a published subject to see its overview and questions."
                    )
                }
            } else {
                item {
                    SubjectDiscoveryOverviewCard(
                        subject = selectedSubject,
                        overview = overview,
                        questionsCount = subjectDiscoveryViewModel.totalCount.takeIf { it > 0 } ?: questions.size,
                        yearsCount = overview?.years?.distinct()?.size ?: questions.mapNotNull { it.year ?: it.examYear }.distinct().size,
                        topicsCount = overview?.topics?.distinct()?.size ?: questions.mapNotNull { it.topic?.trim()?.takeIf { topic -> topic.isNotBlank() } }.distinct().size,
                        paperTypes = selectedSubject.supportedPaperTypes,
                        topicChips = overview?.topics.orEmpty().distinct().takeIf { it.isNotEmpty() }
                            ?: questions.mapNotNull { it.topic?.trim()?.takeIf { topic -> topic.isNotBlank() } }.distinct(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    DiscoveryNextStepsSection(
                        selectedSubjectNavArgs = SelectedSubjectNavArgs(
                            subjectCode = selectedSubject.subjectCode,
                            subjectName = selectedSubject.subjectName,
                            academicLevel = selectedSubject.academicLevel,
                            group = selectedSubject.group,
                            paperType = selectedPaperType
                        ),
                        onOpenSimilarQuestionsClick = onOpenSimilarQuestionsClick,
                        onOpenTopicAnalysisClick = onOpenTopicAnalysisClick,
                        onOpenPredictionsClick = onOpenPredictionsClick,
                        onOpenAnswerBuilderClick = onOpenAnswerBuilderClick
                    )
                }

                item {
                    SubjectPublishedQuestionsHeader(
                        totalCount = subjectDiscoveryViewModel.totalCount.takeIf { it > 0 } ?: questions.size,
                        selectedPaperType = selectedPaperType,
                        supportedPaperTypes = selectedSubject.supportedPaperTypes ?: PaperTypeLookups.allPaperTypes(),
                        onPaperTypeSelected = { paperType ->
                            subjectDiscoveryViewModel.selectPaperType(selectedSubject.subjectCode, paperType)
                        }
                    )
                }

                when {
                    questionsLoading -> {
                        item {
                            QuestionsLoadingState(label = "Loading published questions...")
                        }
                    }

                    errorMessage != null -> {
                        item {
                            QuestionsErrorStateCard(
                                message = errorMessage,
                                onRetry = { subjectDiscoveryViewModel.retrySelectedSubjectLoad() }
                            )
                        }
                    }

                    questions.isEmpty() -> {
                        item {
                            QuestionsEmptyStateCard()
                        }
                    }

                    else -> {
                        itemsIndexed(questions) { index, question ->
                            val fallbackOnGetAnswer = onGetAnswerClick
                            val subjectCode = selectedSubject.subjectCode
                            QuestionCard(
                                question = question,
                                subjectCode = subjectCode,
                                questionIndex = index,
                                onGetAnswerClick = { payload ->
                                    if (fallbackOnGetAnswer != null) {
                                        fallbackOnGetAnswer(payload)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscoverySearchSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    subjects: List<Subject>,
    subjectsLoading: Boolean,
    publishedSubjectsErrorMessage: String?,
    onSearchClick: () -> Unit,
    onBrowseClick: () -> Unit,
    onSubjectSelected: (Subject) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Search subject by code or name") },
                placeholder = { Text(text = "Try Mathematics or 109") },
                singleLine = true,
                trailingIcon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSearchClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = if (subjectsLoading) "Searching..." else "Search subject")
                }

                OutlinedButton(
                    onClick = {
                        expanded = !expanded
                        onBrowseClick()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Browse published subjects")
                }
            }

            if (!publishedSubjectsErrorMessage.isNullOrBlank()) {
                Text(
                    text = publishedSubjectsErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Box {
                if (subjects.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Select a published subject")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = subject.displayLabel(),
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        subject.displaySubtitle()?.let { subtitle ->
                                            Text(
                                                text = subtitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    expanded = false
                                    onSubjectSelected(subject)
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
private fun SubjectDiscoveryOverviewCard(
    subject: Subject,
    overview: SubjectOverviewResponse?,
    questionsCount: Int,
    yearsCount: Int,
    topicsCount: Int,
    paperTypes: List<String>?,
    topicChips: List<String>,
    modifier: Modifier = Modifier
) {
    val supportedPaperTypes = paperTypes.orEmpty().ifEmpty { PaperTypeLookups.allPaperTypes() }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = subject.subjectCode,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = subject.subjectName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                subject.displaySubtitle()?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DiscoveryStatCard(label = "Questions", value = questionsCount.toString(), icon = Icons.Default.Description, modifier = Modifier.weight(1f))
                DiscoveryStatCard(label = "Years", value = yearsCount.toString(), icon = Icons.Default.CalendarMonth, modifier = Modifier.weight(1f))
                DiscoveryStatCard(label = "Topics", value = topicsCount.toString(), icon = Icons.Default.Topic, modifier = Modifier.weight(1f))
            }

            DiscoveryDetailChips(
                academicLevel = subject.academicLevel,
                group = subject.group,
                paperTypes = supportedPaperTypes,
                topicChips = topicChips
            )

            overview?.summary?.trim()?.takeIf { it.isNotBlank() }?.let { summary ->
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DiscoveryDetailChips(
    academicLevel: String?,
    group: String?,
    paperTypes: List<String>,
    topicChips: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            academicLevel?.trim()?.takeIf { it.isNotBlank() }?.let { value ->
                DiscoveryChip(text = value)
            }
            group?.trim()?.takeIf { it.isNotBlank() }?.let { value ->
                DiscoveryChip(text = value)
            }
            paperTypes.forEach { paperType ->
                DiscoveryChip(text = paperType)
            }
        }

        if (topicChips.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Topics",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    topicChips.take(12).forEach { topic ->
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = {
                                Text(
                                    text = topic,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveryChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DiscoveryStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DiscoveryNextStepsSection(
    selectedSubjectNavArgs: SelectedSubjectNavArgs,
    onOpenSimilarQuestionsClick: (SelectedSubjectNavArgs) -> Unit,
    onOpenTopicAnalysisClick: (SelectedSubjectNavArgs) -> Unit,
    onOpenPredictionsClick: (SelectedSubjectNavArgs) -> Unit,
    onOpenAnswerBuilderClick: (SelectedSubjectNavArgs) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Next Steps",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NextStepActionCard(
                title = "Semantic Search",
                description = "Find similar published questions.",
                icon = Icons.Default.Search,
                onClick = { onOpenSimilarQuestionsClick(selectedSubjectNavArgs) },
                modifier = Modifier.weight(1f)
            )
            NextStepActionCard(
                title = "Topic Analysis",
                description = "Review repeated topics and marks.",
                icon = Icons.Default.Insights,
                onClick = { onOpenTopicAnalysisClick(selectedSubjectNavArgs) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NextStepActionCard(
                title = "Predictions",
                description = "See likely exam topics.",
                icon = Icons.Default.AutoAwesome,
                onClick = { onOpenPredictionsClick(selectedSubjectNavArgs) },
                modifier = Modifier.weight(1f)
            )
            NextStepActionCard(
                title = "Answer Help",
                description = "Draft an exam-style answer.",
                icon = Icons.Default.HelpOutline,
                onClick = { onOpenAnswerBuilderClick(selectedSubjectNavArgs) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}