package com.qarena.android.presentation.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.core.analytics.AnalyticsTracker
import com.qarena.android.data.remote.dto.SubjectOverviewResponse
import com.qarena.android.model.displaySubtitle
import com.qarena.android.model.toSuggestion
import com.qarena.android.presentation.common.AnswerPayload
import com.qarena.android.util.QuestionPresentationLookups
import com.qarena.android.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectOverviewScreen(
    subjectCode: String,
    onOpenQuestionsClick: (String) -> Unit,
    onOpenSuggestionsClick: (String) -> Unit,
    onOpenAnalysisClick: (String) -> Unit,
    onOpenPredictionsClick: (String) -> Unit,
    onGetAnswerClick: (AnswerPayload) -> Unit,
    subjectOverviewViewModel: SubjectOverviewViewModel = viewModel()
) {
    SubjectDetailsScreen(
        subjectCode = subjectCode,
        onOpenQuestionsClick = onOpenQuestionsClick,
        onOpenSuggestionsClick = onOpenSuggestionsClick,
        onOpenAnalysisClick = onOpenAnalysisClick,
        onOpenPredictionsClick = onOpenPredictionsClick,
        onGetAnswerClick = onGetAnswerClick,
        subjectOverviewViewModel = subjectOverviewViewModel
    )
}

@Composable
private fun OverviewHeader(
    overview: SubjectOverviewResponse,
    fallbackSubjectCode: String
) {
    val subjectName = overview.subject?.subjectName ?: overview.subjectName ?: "Unnamed subject"
    val subjectCode = overview.subject?.subjectCode ?: overview.subjectCode ?: fallbackSubjectCode
    val totalQuestions = overview.totalQuestions ?: overview.questionCount

    Column {
        Text(
            text = subjectCode,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subjectName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Questions",
                value = totalQuestions?.toString() ?: "0",
                icon = Icons.Default.Description,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Years",
                value = overview.years?.size?.toString() ?: "0",
                icon = Icons.Default.Event,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Topics",
                value = overview.topics?.size?.toString() ?: "0",
                icon = Icons.Default.Topic,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionShortcuts(
    subjectCode: String,
    onOpenSuggestionsClick: (String) -> Unit,
    onOpenAnalysisClick: (String) -> Unit,
    onOpenPredictionsClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Next Steps",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShortcutCard(
                title = "Predictions",
                icon = Icons.Default.AutoAwesome,
                onClick = { onOpenPredictionsClick(subjectCode) },
                modifier = Modifier.weight(1f)
            )
            ShortcutCard(
                title = "Analysis",
                icon = Icons.Default.Insights,
                onClick = { onOpenAnalysisClick(subjectCode) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ShortcutCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(width = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun PublishedQuestionsHeader(
    supportedPaperTypes: List<String>,
    selectedPaperType: String?,
    onPaperTypeSelected: (String) -> Unit
) {
    Column {
        Text(
            text = "Published Questions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (supportedPaperTypes.isNotEmpty()) {
            PaperTypeSelector(
                supportedPaperTypes = supportedPaperTypes,
                selectedPaperType = selectedPaperType,
                onPaperTypeSelected = onPaperTypeSelected
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
