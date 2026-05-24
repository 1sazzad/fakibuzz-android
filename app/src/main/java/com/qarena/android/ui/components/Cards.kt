package com.qarena.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qarena.android.model.Subject
import com.qarena.android.model.Suggestion
import com.qarena.android.model.displayLabel
import com.qarena.android.model.displayMarks
import com.qarena.android.model.displayPaperType
import com.qarena.android.model.displayQuestionText
import com.qarena.android.model.displayScore
import com.qarena.android.model.displaySubtitle
import com.qarena.android.model.displayTopic
import com.qarena.android.presentation.common.DiagramRenderer
import com.qarena.android.presentation.common.toDiagramInfo
import com.qarena.android.util.QuestionPresentationLookups

@Composable
fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subject.subjectCode.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.subjectName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subject.subjectCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            subject.displaySubtitle()?.let { subtitle ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuestionCard(
    suggestion: Suggestion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paperType = suggestion.displayPaperType()
    val isPrediction = suggestion.predictionScore != null
    val normalizedPaperType = paperType?.uppercase()

    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (paperType != null) {
                    Badge(
                        containerColor = when (paperType.uppercase()) {
                            "MCQ" -> MaterialTheme.colorScheme.secondaryContainer
                            "WRITTEN" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        },
                        contentColor = when (paperType.uppercase()) {
                            "MCQ" -> MaterialTheme.colorScheme.onSecondaryContainer
                            "WRITTEN" -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = paperType,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                if (isPrediction) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Predicted",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            QuestionCardBody(
                suggestion = suggestion,
                normalizedPaperType = normalizedPaperType
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                suggestion.displayTopic()?.let { topic ->
                    InfoChip(text = topic, icon = null)
                }
                suggestion.displayMarks()?.let { marks ->
                    InfoChip(text = "$marks Marks", icon = null)
                }
                (suggestion.examYear ?: suggestion.year)?.let { year ->
                    InfoChip(text = year.toString(), icon = null)
                }
            }
        }
    }
}

@Composable
private fun QuestionCardBody(
    suggestion: Suggestion,
    normalizedPaperType: String?
) {
    Text(
        text = suggestion.displayQuestionText(),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        maxLines = 4,
        overflow = TextOverflow.Ellipsis
    )

    suggestion.stem?.trim()?.takeIf { it.isNotBlank() && it != suggestion.displayQuestionText() }?.let { stem ->
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stem,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    DiagramRenderer(diagramInfo = suggestion.toDiagramInfo())

    when (normalizedPaperType) {
        "MCQ" -> {
            QuestionSectionTitle(text = "Options")
            suggestion.options.orEmpty().forEachIndexed { index, option ->
                option.trim().takeIf { it.isNotBlank() }?.let { text ->
                    Text(
                        text = "${index + 1}. $text",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        "CQ" -> {
            suggestion.marks?.let { marks ->
                QuestionSectionTitle(text = "Marks: $marks")
            }

            suggestion.subQuestions.orEmpty().forEachIndexed { index, subQuestion ->
                val label = subQuestion.label?.trim()?.takeIf { it.isNotBlank() } ?: "${index + 1}."
                val text = QuestionPresentationLookups.displayQuestionText(subQuestion.questionText)

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$label $text",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                subQuestion.answerHint?.trim()?.takeIf { it.isNotBlank() }?.let { hint ->
                    Text(
                        text = "Hint: $hint",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        "WRITTEN" -> {
            suggestion.instruction?.trim()?.takeIf { it.isNotBlank() }?.let { instruction ->
                QuestionSectionTitle(text = "Instruction")
                Text(text = instruction, style = MaterialTheme.typography.bodyMedium)
            }

            suggestion.section?.trim()?.takeIf { it.isNotBlank() }?.let { section ->
                Text(
                    text = "Section: $section",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            suggestion.questionType?.trim()?.takeIf { it.isNotBlank() }?.let { questionType ->
                Text(
                    text = "Question type: $questionType",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            suggestion.tableData?.takeIf { !it.isJsonNull }?.let { tableData ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Table data: ${QuestionPresentationLookups.formatJsonContent(tableData)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            suggestion.wordBox?.takeIf { !it.isJsonNull }?.let { wordBox ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Word box: ${QuestionPresentationLookups.formatJsonContent(wordBox)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuestionSectionTitle(text: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun InfoChip(
    text: String,
    icon: @Composable (() -> Unit)?,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.invoke()
            if (icon != null) Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
