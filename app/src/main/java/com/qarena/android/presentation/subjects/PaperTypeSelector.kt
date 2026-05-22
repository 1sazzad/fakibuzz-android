package com.qarena.android.presentation.subjects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qarena.android.util.PaperTypeLookups

@Composable
fun PaperTypeSelector(
    supportedPaperTypes: List<String>,
    selectedPaperType: String?,
    onPaperTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val normalizedSupportedPaperTypes = PaperTypeLookups.normalizeSupportedPaperTypes(supportedPaperTypes)

    if (normalizedSupportedPaperTypes.isEmpty()) {
        return
    }

    val resolvedSelectedPaperType = PaperTypeLookups.resolveSelectedPaperType(
        preferredPaperType = selectedPaperType,
        supportedPaperTypes = normalizedSupportedPaperTypes
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        normalizedSupportedPaperTypes.forEach { paperType ->
            FilterChip(
                selected = paperType == resolvedSelectedPaperType,
                onClick = { onPaperTypeSelected(paperType) },
                label = { Text(text = paperType) }
            )
        }
    }
}

@Composable
fun PaperTypeBadge(
    paperType: String?,
    modifier: Modifier = Modifier
) {
    val normalizedPaperType = PaperTypeLookups.normalizePaperType(paperType) ?: return

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = normalizedPaperType,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}