@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.qarena.android.presentation.boardpapers

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.qarena.android.data.remote.dto.BoardPaperDetailResponse
import com.qarena.android.data.remote.dto.BoardPaperExam
import com.qarena.android.data.remote.dto.Question
import com.qarena.android.data.remote.dto.TableData
import com.qarena.android.data.remote.dto.WordBox
import com.qarena.android.model.SubQuestion
import com.qarena.android.presentation.common.DiagramInfo
import com.qarena.android.presentation.common.DiagramRenderer
import com.qarena.android.ui.components.QArenaEmptyState
import com.qarena.android.ui.components.QArenaLoadingState
import com.qarena.android.util.QuestionPresentationLookups
import java.util.regex.Pattern

@Composable
fun PaperRenderer(
    paper: BoardPaperDetailResponse,
    modifier: Modifier = Modifier
) {
    val exam = paper.exam

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PaperHeaderCard(exam = exam)

        if (paper.questions.isEmpty()) {
            QArenaEmptyState(
                title = "No questions found",
                description = "This paper did not return any questions.",
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                paper.questions.forEachIndexed { index, question ->
                    QuestionRenderer(
                        question = question,
                        index = index + 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun PaperHeaderCard(
    exam: BoardPaperExam?,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = exam?.examName?.takeIf { it.isNotBlank() } ?: "Board Paper",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                val boardLine = buildList {
                    exam?.boardName?.takeIf { it.isNotBlank() }?.let { add("$it Board") }
                    exam?.examYear?.let { add(it.toString()) }
                }.joinToString(" • ")

                if (boardLine.isNotBlank()) {
                    Text(
                        text = boardLine,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                paperInfoChip(exam?.subjectName?.takeIf { it.isNotBlank() } ?: "Subject")
                exam?.subjectCode?.takeIf { it.isNotBlank() }?.let { paperInfoChip(it) }
                exam?.paperType?.takeIf { it.isNotBlank() }?.let { paperInfoChip(it) }
                exam?.time?.takeIf { it.isNotBlank() }?.let { paperInfoChip(it) }
                exam?.totalMarks?.let { paperInfoChip("$it marks") }
                exam?.group?.takeIf { it.isNotBlank() }?.let { paperInfoChip(it) }
            }
        }
    }
}

@Composable
fun QuestionRenderer(
    question: Question,
    index: Int,
    modifier: Modifier = Modifier
) {
    val questionNumber = question.questionNo?.trim()?.takeIf { it.isNotBlank() } ?: index.toString()

    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question $questionNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                question.marks?.let { marks ->
                    paperInfoChip(QuestionPresentationLookups.formatMarks(marks) ?: marks.toString())
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                question.section?.trim()?.takeIf { it.isNotBlank() }?.let { paperInfoChip("Section: $it") }
                question.questionType?.trim()?.takeIf { it.isNotBlank() }?.let { paperInfoChip(it) }
                question.topic?.trim()?.takeIf { it.isNotBlank() }?.let { paperInfoChip("Topic: $it") }
            }

            question.instruction?.trim()?.takeIf { it.isNotBlank() }?.let { instruction ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
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

            question.questionText?.trim()?.takeIf { it.isNotBlank() }?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )
            }

            WordBoxRenderer(wordBox = question.wordBox)
            TableDataRenderer(tableData = question.tableData)
            SvgDiagramRenderer(
                diagramType = question.diagramType,
                diagramSvg = question.diagramSvg,
                diagramRequired = question.diagramRequired,
                diagramDescription = question.diagramDescription
            )

            question.subQuestions.orEmpty().takeIf { it.isNotEmpty() }?.let { subQuestions ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Text(
                        text = "Sub-questions",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    subQuestions.forEach { subQuestion ->
                        SubQuestionRenderer(subQuestion = subQuestion)
                    }
                }
            }
        }
    }
}

@Composable
fun SubQuestionRenderer(
    subQuestion: SubQuestion,
    modifier: Modifier = Modifier
) {
    val resolvedLabel = resolveSubQuestionLabel(subQuestion)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = resolvedLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                QuestionPresentationLookups.formatMarks(subQuestion.marks)?.let { marks ->
                    paperInfoChip("$marks marks")
                }
            }

            subQuestion.questionText?.trim()?.takeIf { it.isNotBlank() }?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun TableDataRenderer(
    tableData: TableData?,
    modifier: Modifier = Modifier
) {
    val columns = tableData?.columns.orEmpty()
    val rows = tableData?.rows.orEmpty()

    if (columns.isEmpty() && rows.isEmpty()) {
        return
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Table",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (columns.isNotEmpty()) {
                TableRow(cells = columns, isHeader = true)
                Spacer(modifier = Modifier.height(6.dp))
            }

            rows.forEachIndexed { index, row ->
                TableRow(cells = row, isHeader = false)
                if (index < rows.lastIndex) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
fun WordBoxRenderer(
    wordBox: WordBox?,
    modifier: Modifier = Modifier
) {
    val words = wordBox?.words.orEmpty().filter { it.isNotBlank() }

    if (words.isEmpty()) {
        return
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Word Box",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                words.forEach { word ->
                    paperInfoChip(word)
                }
            }
        }
    }
}

@Composable
fun SvgDiagramRenderer(
    diagramType: String?,
    diagramSvg: String?,
    diagramRequired: Boolean?,
    diagramDescription: String?,
    modifier: Modifier = Modifier
) {
    val hasSvgDiagram = diagramType?.trim()?.equals("svg", ignoreCase = true) == true && !diagramSvg.isNullOrBlank()

    if (hasSvgDiagram) {
        DiagramRenderer(
            diagramInfo = DiagramInfo(
                diagramRequired = diagramRequired,
                diagramType = diagramType,
                diagramSvg = diagramSvg,
                diagramDescription = diagramDescription
            ),
            modifier = modifier
        )
        return
    }

    val fallbackText = diagramDescription?.trim()?.takeIf { it.isNotBlank() }

    if (diagramRequired == true) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Diagram",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = fallbackText ?: "Diagram available but cannot be displayed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BoardPaperLoadingState(
    label: String = "Loading board papers..."
) {
    QArenaLoadingState(label = label)
}

private fun resolveSubQuestionLabel(subQuestion: SubQuestion): String {
    subQuestion.displayLabel?.trim()?.takeIf { it.isNotBlank() }?.let { return it }

    val questionNo = subQuestion.questionNo?.trim()?.takeIf { it.isNotBlank() }
    if (!questionNo.isNullOrBlank()) {
        val matcher = Pattern.compile("\\([^()]+\\)").matcher(questionNo)
        if (matcher.find()) {
            return matcher.group()
        }
    }

    return subQuestion.label?.trim()?.takeIf { it.isNotBlank() } ?: ""
}

@Composable
private fun TableRow(
    cells: List<String>,
    isHeader: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        cells.forEach { cell ->
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = if (isHeader) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                contentColor = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Text(
                    text = cell,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
                    fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun paperInfoChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}