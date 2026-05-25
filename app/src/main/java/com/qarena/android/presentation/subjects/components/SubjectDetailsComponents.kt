@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.qarena.android.presentation.subjects.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qarena.android.data.remote.dto.QuestionResponse
import com.qarena.android.data.remote.dto.SubjectOverviewResponse
import com.qarena.android.model.Subject
import com.qarena.android.model.SubQuestion
import com.qarena.android.model.displaySubtitle
import com.qarena.android.presentation.common.AnswerPayload
import com.qarena.android.presentation.common.DiagramRenderer
import com.qarena.android.presentation.common.toDiagramInfo
import com.qarena.android.util.PaperTypeLookups
import com.qarena.android.util.QuestionPresentationLookups

@Composable
fun SubjectHeaderSection(
    subject: Subject?,
    overview: SubjectOverviewResponse,
    fallbackSubjectCode: String,
    modifier: Modifier = Modifier
) {
    val subjectCode = subject?.subjectCode?.takeIf { it.isNotBlank() }
        ?: overview.subject?.subjectCode?.takeIf { it.isNotBlank() }
        ?: overview.subjectCode?.takeIf { it.isNotBlank() }
        ?: fallbackSubjectCode
    val subjectName = subject?.subjectName?.takeIf { it.isNotBlank() }
        ?: overview.subject?.subjectName?.takeIf { it.isNotBlank() }
        ?: overview.subjectName?.takeIf { it.isNotBlank() }
        ?: "Subject Details"
    val subjectSubtitle = subject?.displaySubtitle()?.takeIf { it.isNotBlank() }
        ?: overview.summary?.trim()?.takeIf { it.isNotBlank() }
    val questionCount = overview.totalQuestions ?: overview.questionCount ?: 0
    val yearsCount = overview.years.orEmpty().distinct().size
    val topicsCount = overview.topics.orEmpty().distinct().size

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = subjectCode,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = subjectName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                subjectSubtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SubjectStatsCard(
                    label = "Questions",
                    value = questionCount.toString(),
                    icon = Icons.Default.Description,
                    modifier = Modifier.weight(1f)
                )
                SubjectStatsCard(
                    label = "Years",
                    value = yearsCount.toString(),
                    icon = Icons.Default.Event,
                    modifier = Modifier.weight(1f)
                )
                SubjectStatsCard(
                    label = "Topics",
                    value = topicsCount.toString(),
                    icon = Icons.Default.Topic,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SubjectStatsCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(20.dp)
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
fun NextStepActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PaperTypeTabs(
    supportedPaperTypes: List<String>,
    selectedPaperType: String?,
    onPaperTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val normalizedPaperTypes = PaperTypeLookups.normalizeSupportedPaperTypes(supportedPaperTypes)
        .ifEmpty { PaperTypeLookups.allPaperTypes() }
    val resolvedSelectedPaperType = PaperTypeLookups.resolveSelectedPaperType(
        preferredPaperType = selectedPaperType,
        supportedPaperTypes = normalizedPaperTypes
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        normalizedPaperTypes.forEach { paperType ->
            FilterChip(
                selected = paperType == resolvedSelectedPaperType,
                onClick = { onPaperTypeSelected(paperType) },
                modifier = Modifier.weight(1f),
                label = {
                    Text(
                        text = paperType,
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun SubjectPublishedQuestionsHeader(
    totalCount: Int,
    selectedPaperType: String?,
    supportedPaperTypes: List<String>,
    onPaperTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Published Questions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TotalCountBadge(totalCount = totalCount)
        }

        PaperTypeTabs(
            supportedPaperTypes = supportedPaperTypes,
            selectedPaperType = selectedPaperType,
            onPaperTypeSelected = onPaperTypeSelected
        )
    }
}

@Composable
fun TotalCountBadge(
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Text(
            text = "$totalCount total",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuestionCard(
    question: QuestionResponse,
    subjectCode: String,
    questionIndex: Int,
    onGetAnswerClick: (AnswerPayload) -> Unit,
    modifier: Modifier = Modifier
) {
    val normalizedPaperType = PaperTypeLookups.normalizePaperType(question.paperType)
    val questionNumber = question.questionNo?.trim()?.takeIf { it.isNotBlank() }
        ?: (questionIndex + 1).toString()
    val questionTitle = "Question $questionNumber"
    val questionBody = displayQuestionBody(question)
    val sourceLabel = question.section?.trim()?.takeIf { it.isNotBlank() }
    val yearLabel = question.year ?: question.examYear
    val marksLabel = QuestionPresentationLookups.formatMarks(question.marks)

    ElevatedCard(
        onClick = { onGetAnswerClick(question.toAnswerPayload(subjectCode)) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = question.subjectCode?.takeIf { it.isNotBlank() } ?: subjectCode,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    marksLabel?.let {
                        QuestionPill(
                            text = "$it marks",
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    normalizedPaperType?.let {
                        QuestionPill(
                            text = it,
                            containerColor = paperTypeContainerColor(it),
                            contentColor = paperTypeContentColor(it)
                        )
                    }
                }
            }

            Text(
                text = questionTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            questionBody?.let { bodyText ->
                Text(
                    text = bodyText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )
            }

            SvgDiagramCard(
                diagramInfo = question.toDiagramInfo(),
                diagramReference = question.diagramReference,
                modifier = Modifier.fillMaxWidth()
            )

            when (normalizedPaperType) {
                PaperTypeLookups.MCQ -> {
                    McqOptionsGrid(options = question.options.orEmpty())
                }

                PaperTypeLookups.CQ -> {
                    CqSubQuestionList(subQuestions = question.subQuestions.orEmpty())
                }

                PaperTypeLookups.WRITTEN -> {
                    WrittenQuestionBody(question = question)
                }
            }

            MetadataChipRow(
                source = sourceLabel,
                year = yearLabel,
                topic = question.topic?.trim()?.takeIf { it.isNotBlank() },
                diagramRequired = question.diagramRequired == true
            )
        }
    }
}

@Composable
fun McqOptionsGrid(
    options: List<String>,
    modifier: Modifier = Modifier
) {
    if (options.isEmpty()) {
        return
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.chunked(2).forEach { optionRow ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                optionRow.forEachIndexed { rowIndex, option ->
                    McqOptionCard(
                        index = rowIndex,
                        option = option,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (optionRow.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun McqOptionCard(
    index: Int,
    option: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text(
                    text = (index + 1).toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = option.trim(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CqSubQuestionList(
    subQuestions: List<SubQuestion>,
    modifier: Modifier = Modifier
) {
    if (subQuestions.isEmpty()) {
        return
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        subQuestions.forEachIndexed { index, subQuestion ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = QuestionPresentationLookups.subQuestionLabel(index, subQuestion.label),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        QuestionPresentationLookups.formatMarks(subQuestion.marks)?.let { marks ->
                            QuestionPill(
                                text = "$marks marks",
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    subQuestion.questionText?.trim()?.takeIf { it.isNotBlank() }?.let { text ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WrittenQuestionBody(
    question: QuestionResponse,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        question.instruction?.trim()?.takeIf { it.isNotBlank() }?.let { instruction ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Text(
                    text = instruction,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        question.tableData?.takeIf { !it.isJsonNull }?.let { tableData ->
            DetailTextCard(
                label = "Table data",
                content = QuestionPresentationLookups.formatJsonContent(tableData)
            )
        }

        question.wordBox?.takeIf { !it.isJsonNull }?.let { wordBox ->
            DetailTextCard(
                label = "Word box",
                content = QuestionPresentationLookups.formatJsonContent(wordBox)
            )
        }
    }
}

@Composable
private fun DetailTextCard(
    label: String,
    content: String?,
    modifier: Modifier = Modifier
) {
    val text = content?.trim()?.takeIf { it.isNotBlank() } ?: return

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MetadataChipRow(
    source: String? = null,
    year: Int? = null,
    topic: String? = null,
    diagramRequired: Boolean = false,
    modifier: Modifier = Modifier
) {
    val chips = buildList {
        if (diagramRequired) {
            add("Diagram Required")
        }
        source?.takeIf { it.isNotBlank() }?.let { add("Source: $it") }
        year?.let { add("Year: $it") }
        topic?.takeIf { it.isNotBlank() }?.let { add("Topic: $it") }
    }

    if (chips.isEmpty()) {
        return
    }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chipText ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Text(
                    text = chipText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SvgDiagramCard(
    diagramInfo: com.qarena.android.presentation.common.DiagramInfo?,
    diagramReference: String? = null,
    modifier: Modifier = Modifier
) {
    val diagramReferenceText = diagramReference?.trim()?.takeIf { it.isNotBlank() }
        ?: diagramInfo?.diagramReference?.trim()?.takeIf { it.isNotBlank() }
    val hasDiagramContent = diagramInfo?.diagramRequired == true ||
        !diagramInfo?.diagramSvg.isNullOrBlank() ||
        !diagramInfo?.diagramUrl.isNullOrBlank() ||
        !diagramInfo?.diagramDescription.isNullOrBlank() ||
        !diagramReferenceText.isNullOrBlank()

    if (!hasDiagramContent) {
        return
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                DiagramRenderer(
                    diagramInfo = diagramInfo,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            diagramReferenceText?.let { reference ->
                Text(
                    text = "Reference: $reference",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuestionsLoadingState(
    modifier: Modifier = Modifier,
    label: String = "Loading questions..."
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                strokeWidth = 2.5.dp,
                modifier = Modifier.width(22.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        repeat(3) {
            QuestionSkeletonCard()
        }
    }
}

@Composable
fun QuestionsEmptyStateCard(
    modifier: Modifier = Modifier,
    title: String = "No published questions found",
    description: String = "Try another paper type or check back later."
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuestionsErrorStateCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Could not load questions"
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "Retry")
            }
        }
    }
}

@Composable
fun QuestionSkeletonCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonPill(width = 80.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonPill(width = 52.dp)
                    SkeletonPill(width = 56.dp)
                }
            }
            SkeletonLine(widthFraction = 0.42f, height = 18.dp)
            SkeletonLine(widthFraction = 0.92f, height = 14.dp)
            SkeletonLine(widthFraction = 0.7f, height = 14.dp)
            SkeletonBox(height = 112.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonPill(width = 78.dp)
                SkeletonPill(width = 64.dp)
                SkeletonPill(width = 84.dp)
            }
        }
    }
}

@Composable
private fun SkeletonLine(
    widthFraction: Float,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    )
}

@Composable
private fun SkeletonBox(
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    )
}

@Composable
private fun SkeletonPill(
    width: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(width)
            .height(24.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    )
}

@Composable
private fun QuestionPill(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun displayQuestionBody(question: QuestionResponse): String? {
    val questionText = question.questionText?.trim()?.takeIf { it.isNotBlank() }
    val stemText = question.stem?.trim()?.takeIf { it.isNotBlank() }
    return questionText ?: stemText
}

private fun QuestionResponse.toAnswerPayload(subjectCode: String): AnswerPayload {
    val questionTextForPrompt = questionText?.trim()?.takeIf { it.isNotBlank() }
        ?: stem?.trim()?.takeIf { it.isNotBlank() }
        ?: ""

    return AnswerPayload(
        questionId = id,
        questionText = questionTextForPrompt,
        prompt = QuestionPresentationLookups.buildAnswerPrompt(
            questionText = questionText,
            section = section,
            questionType = questionType,
            instruction = instruction,
            stem = stem,
            tableData = tableData,
            wordBox = wordBox,
            options = options,
            subQuestions = subQuestions,
            formulaLatex = formulaLatex,
            formulaDisplay = formulaDisplay,
            diagramDescription = diagramDescription,
            diagramReference = diagramReference,
            diagramType = diagramType,
            diagramSvg = diagramSvg,
            diagramRequired = diagramRequired,
            mathBlocks = mathBlocks
        ),
        subjectCode = subjectCode,
        academicLevel = academicLevel,
        paperType = paperType,
        topic = topic,
        marks = marks,
        formulaLatex = formulaLatex,
        formulaDisplay = formulaDisplay,
        diagramRequired = diagramRequired,
        diagramType = diagramType,
        diagramSvg = diagramSvg,
        diagramUrl = diagramUrl,
        diagramReference = diagramReference,
        diagramDescription = diagramDescription,
        mathBlocks = mathBlocks,
        answerType = answerType,
        section = section,
        instruction = instruction,
        stem = stem,
        questionType = questionType,
        tableData = tableData,
        wordBox = wordBox
    )
}

@Composable
private fun paperTypeContainerColor(paperType: String): androidx.compose.ui.graphics.Color {
    return when (paperType.uppercase()) {
        PaperTypeLookups.MCQ -> MaterialTheme.colorScheme.secondaryContainer
        PaperTypeLookups.CQ -> MaterialTheme.colorScheme.tertiaryContainer
        PaperTypeLookups.WRITTEN -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
}

@Composable
private fun paperTypeContentColor(paperType: String): androidx.compose.ui.graphics.Color {
    return when (paperType.uppercase()) {
        PaperTypeLookups.MCQ -> MaterialTheme.colorScheme.onSecondaryContainer
        PaperTypeLookups.CQ -> MaterialTheme.colorScheme.onTertiaryContainer
        PaperTypeLookups.WRITTEN -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
