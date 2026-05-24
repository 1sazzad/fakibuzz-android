package com.qarena.android.presentation.subjects

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.qarena.android.core.analytics.AnalyticsTracker
import com.qarena.android.data.remote.dto.SubjectAnalysisResponse
import com.qarena.android.model.displayLabel
import com.qarena.android.model.displaySubtitle
import com.qarena.android.presentation.common.DiagramInfo
import com.qarena.android.presentation.common.DiagramRenderer
import com.qarena.android.presentation.subjects.PaperTypeSelector
import com.qarena.android.util.PaperTypeLookups

@Composable
fun SubjectAnalysisScreen(
    subjectCode: String,
    subjectAnalysisViewModel: SubjectAnalysisViewModel = viewModel()
) {
    val analysisState = subjectAnalysisViewModel.analysisState
    val subject = subjectAnalysisViewModel.subject
    val supportedPaperTypes = subjectAnalysisViewModel.supportedPaperTypes
    val selectedPaperType = subjectAnalysisViewModel.selectedPaperType

    LaunchedEffect(subjectCode) {
        AnalyticsTracker.trackScreen(
            screenName = "Analysis",
            path = "/android/subjects/$subjectCode/analysis",
            subjectCode = subjectCode
        )
        subjectAnalysisViewModel.loadSubjectAnalysis(subjectCode)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Analysis",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subject?.displayLabel() ?: subjectCode,
                fontSize = 16.sp
            )

            subject?.displaySubtitle()?.let { subtitle ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (supportedPaperTypes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                PaperTypeSelector(
                    supportedPaperTypes = supportedPaperTypes,
                    selectedPaperType = selectedPaperType,
                    onPaperTypeSelected = { paperType ->
                        subjectAnalysisViewModel.selectPaperType(subjectCode, paperType)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (analysisState) {
                SubjectAnalysisUiState.Idle,
                SubjectAnalysisUiState.Loading -> {
                    Text(text = "Loading subject analysis...")
                }

                is SubjectAnalysisUiState.Error -> {
                    Text(
                        text = analysisState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is SubjectAnalysisUiState.Success -> {
                    SubjectAnalysisContent(
                        analysis = analysisState.analysis,
                        fallbackSubjectCode = subjectCode
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectAnalysisContent(
    analysis: SubjectAnalysisResponse,
    fallbackSubjectCode: String
) {
    val subjectCode = analysis.subjectCode ?: fallbackSubjectCode
    val subjectName = analysis.subjectName ?: "Subject analysis"
    val sections = listOf(
        AnalysisSection("Topic-wise analysis", analysis.topics.toDisplayRows()),
        AnalysisSection("Years", analysis.years.toDisplayRows()),
        AnalysisSection("Marks distribution", analysis.marks.toDisplayRows()),
        AnalysisSection("Sample questions", analysis.samples.toDisplayRows())
    )

    if (
        analysis.subjectCode == null &&
        analysis.subjectName == null &&
        analysis.totalQuestions == null &&
        analysis.summary.isNullOrBlank() &&
        sections.all { it.rows.isEmpty() }
    ) {
        Text(text = analysis.message ?: analysis.status ?: "No analysis found")
        return
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = subjectName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subjectCode,
                fontSize = 16.sp
            )

            analysis.totalQuestions?.let { totalQuestions ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Total questions: $totalQuestions",
                    fontSize = 14.sp
                )
            }

            analysis.summary?.takeIf { it.isNotBlank() }?.let { summary ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = summary)
            }

            analysis.status?.takeIf { it.isNotBlank() }?.let { status ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Status: $status",
                    fontSize = 14.sp
                )
            }
        }
    }

    sections.forEach { section ->
        AnalysisRowsSection(
            title = section.title,
            rows = section.rows
        )
    }

    AnalysisSamplesSection(samples = analysis.samples)
}

@Composable
private fun AnalysisRowsSection(
    title: String,
    rows: List<String>
) {
    if (rows.isEmpty()) {
        return
    }

    Spacer(modifier = Modifier.height(16.dp))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            rows.forEach { row ->
                Text(
                    text = row,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

private data class AnalysisSection(
    val title: String,
    val rows: List<String>
)

private fun JsonElement?.toDisplayRows(): List<String> {
    if (this == null || isJsonNull) {
        return emptyList()
    }

    return when {
        isJsonArray -> asJsonArray.mapNotNull { item -> item.toDisplayRow() }
        else -> listOfNotNull(toDisplayRow())
    }
}

@Composable
private fun AnalysisSamplesSection(samples: JsonElement?) {
    if (samples == null || samples.isJsonNull || !samples.isJsonArray) {
        return
    }

    val sampleItems = samples.asJsonArray.mapNotNull { element ->
        element.takeIf { it.isJsonObject }?.asJsonObject
    }

    if (sampleItems.isEmpty()) {
        return
    }

    Spacer(modifier = Modifier.height(16.dp))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Sample questions",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            sampleItems.forEach { sample ->
                val text = sample.firstString(
                    "question_text",
                    "stem",
                    "question",
                    "title",
                    "label"
                )
                val diagramInfo = DiagramInfo(
                    diagramRequired = sample.firstBoolean("diagram_required"),
                    diagramType = sample.firstString("diagram_type"),
                    diagramSvg = sample.firstString("diagram_svg"),
                    diagramUrl = sample.firstString("diagram_url"),
                    diagramDescription = sample.firstString("diagram_description"),
                    diagramReference = sample.firstString("diagram_reference")
                )

                text?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, fontSize = 14.sp)
                }

                DiagramRenderer(diagramInfo = diagramInfo)

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

private fun JsonElement.toDisplayRow(): String? {
    if (isJsonNull) {
        return null
    }

    if (isJsonPrimitive) {
        return asJsonPrimitive.asString.takeIf { it.isNotBlank() }
    }

    if (!isJsonObject) {
        return null
    }

    val json = asJsonObject
    val primary = json.firstString(
        "topic_name",
        "topic",
        "question_text",
        "question",
        "year",
        "marks",
        "label",
        "name"
    )
    val count = json.firstString("count", "total", "total_questions")
    val score = json.firstString("score", "prediction_score")

    return listOfNotNull(
        primary,
        count?.let { "Count: $it" },
        score?.let { "Score: $it" }
    ).joinToString(" - ").takeIf { it.isNotBlank() }
        ?: json.entrySet().joinToString(", ") { entry ->
            "${entry.key}: ${entry.value.simpleValue()}"
        }.takeIf { it.isNotBlank() }
}

private fun JsonObject.firstString(vararg keys: String): String? {
    keys.forEach { key ->
        val value = get(key)?.simpleValue()

        if (!value.isNullOrBlank()) {
            return value
        }
    }

    return null
}

private fun JsonObject.firstBoolean(vararg keys: String): Boolean? {
    keys.forEach { key ->
        val value = get(key)

        if (value != null && !value.isJsonNull) {
            return when {
                value.isJsonPrimitive && value.asJsonPrimitive.isBoolean -> value.asBoolean
                value.isJsonPrimitive && value.asJsonPrimitive.isString -> value.asString.toBooleanStrictOrNull()
                else -> null
            }
        }
    }

    return null
}

private fun String.toBooleanStrictOrNull(): Boolean? {
    return when (lowercase()) {
        "true" -> true
        "false" -> false
        else -> null
    }
}

private fun JsonElement.simpleValue(): String? {
    return when {
        isJsonNull -> null
        isJsonPrimitive -> asJsonPrimitive.asString
        else -> toString()
    }
}
