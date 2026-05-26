@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.qarena.android.presentation.boardpapers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qarena.android.util.AcademicProfile
import com.qarena.android.ui.components.QArenaEmptyState
import com.qarena.android.ui.components.QArenaErrorState
import com.qarena.android.ui.components.QArenaLoadingState
import com.qarena.android.ui.components.QArenaPrimaryButton

@Composable
fun BoardPapersScreen(
    boardPaperViewModel: BoardPaperViewModel = viewModel()
) {
    val uiState = boardPaperViewModel.uiState
    val availablePapers = uiState.availablePapers
    val loadedPapers = uiState.loadedPapers
    val isLoading = uiState.isLoadingAvailable || uiState.isLoadingPaper
    val errorMessage = uiState.errorMessage
    val showGroupedFullPaper = uiState.selectedPaperType == "FULL" && loadedPapers.size > 1
    val academicLevelOptions = remember(uiState.selectedAcademicLevel) {
        uiState.selectedAcademicLevel
            .trim()
            .takeIf { it.isNotBlank() }
            ?.let { level ->
                listOf(
                    FilterOption(
                        label = AcademicProfile.academicLevelLabel(level),
                        value = level
                    )
                )
            }
            .orEmpty()
    }

    LaunchedEffect(availablePapers, uiState.subjectOptions, uiState.boardOptions, uiState.yearOptions, uiState.paperTypeOptions) {
        Log.d("BoardPapers", "availablePapers=${availablePapers.size}")
        Log.d("BoardPapers", "subjectOptions=${uiState.subjectOptions}")
        Log.d("BoardPapers", "boardOptions=${uiState.boardOptions}")
        Log.d("BoardPapers", "yearOptions=${uiState.yearOptions}")
        Log.d("BoardPapers", "paperTypeOptions=${uiState.paperTypeOptions}")
    }

    LaunchedEffect(
        uiState.selectedSubjectCode,
        uiState.selectedBoardName,
        uiState.selectedYear,
        uiState.selectedPaperType
    ) {
        Log.d("BoardPapers", "selectedSubjectCode=${uiState.selectedSubjectCode}")
        Log.d("BoardPapers", "selectedBoardName=${uiState.selectedBoardName}")
        Log.d("BoardPapers", "selectedYear=${uiState.selectedYear}")
        Log.d("BoardPapers", "selectedPaperType=${uiState.selectedPaperType}")
    }

    val canViewPaper = uiState.selectedAcademicLevel.isNotBlank() &&
        uiState.selectedSubjectCode?.isNotBlank() == true &&
        uiState.selectedBoardName?.isNotBlank() == true &&
        uiState.selectedYear != null &&
        uiState.selectedPaperType?.isNotBlank() == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Board Paper Viewer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Academic Level → Subject → Board → Year → Paper Type → View Paper",
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
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BoardPaperSummaryCard(
                    title = "Available Papers",
                    value = availablePapers.size.toString(),
                    iconLabel = "A"
                )
            }

            item {
                BoardPaperSummaryCard(
                    title = "Loaded Results",
                    value = loadedPapers.size.toString(),
                    iconLabel = "L"
                )
            }

            uiState.infoMessage?.let { message ->
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                BoardPaperFiltersCard(
                    academicLevelOptions = academicLevelOptions,
                    subjectOptions = uiState.subjectOptions,
                    boardOptions = uiState.boardOptions,
                    yearOptions = uiState.yearOptions,
                    paperTypeOptions = uiState.paperTypeOptions,
                    selectedAcademicLevel = uiState.selectedAcademicLevel,
                    selectedSubjectCode = uiState.selectedSubjectCode,
                    selectedBoardName = uiState.selectedBoardName,
                    selectedYear = uiState.selectedYear,
                    selectedPaperType = uiState.selectedPaperType,
                    isLoading = isLoading,
                    canViewPaper = canViewPaper,
                    onAcademicLevelSelected = { boardPaperViewModel.selectAcademicLevel(it) },
                    onSubjectSelected = { boardPaperViewModel.selectSubjectCode(it) },
                    onBoardSelected = { boardPaperViewModel.selectBoardName(it) },
                    onYearSelected = { boardPaperViewModel.selectYear(it) },
                    onPaperTypeSelected = { boardPaperViewModel.selectPaperType(it) },
                    onViewPaperClick = { boardPaperViewModel.viewPaper() }
                )
            }

            if (isLoading && loadedPapers.isEmpty()) {
                item {
                    QArenaLoadingState(label = "Loading board papers...")
                }
            }

            errorMessage?.let { message ->
                item {
                    QArenaErrorState(
                        message = message,
                        onRetry = { boardPaperViewModel.loadAvailablePapers(uiState.selectedAcademicLevel) }
                    )
                }
            }

            if (!isLoading && loadedPapers.isEmpty() && errorMessage.isNullOrBlank()) {
                item {
                    QArenaEmptyState(
                        title = "No paper loaded yet",
                        description = "Choose a subject, board, year, and paper type to render the paper."
                    )
                }
            }

            if (showGroupedFullPaper) {
                groupLoadedPapers(loadedPapers).forEach { group ->
                    item {
                        GroupHeader(title = group.first)
                    }
                    items(group.second) { paper ->
                        PaperRenderer(paper = paper)
                    }
                }
            } else {
                items(loadedPapers) { paper ->
                    PaperRenderer(paper = paper)
                }
            }
        }
    }
}

@Composable
private fun BoardPaperSummaryCard(
    title: String,
    value: String,
    iconLabel: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BoardPaperFiltersCard(
    academicLevelOptions: List<FilterOption<String>>,
    subjectOptions: List<FilterOption<String>>,
    boardOptions: List<FilterOption<String>>,
    yearOptions: List<FilterOption<Int>>,
    paperTypeOptions: List<FilterOption<String>>,
    selectedAcademicLevel: String,
    selectedSubjectCode: String?,
    selectedBoardName: String?,
    selectedYear: Int?,
    selectedPaperType: String?,
    isLoading: Boolean,
    canViewPaper: Boolean,
    onAcademicLevelSelected: (String) -> Unit,
    onSubjectSelected: (String) -> Unit,
    onBoardSelected: (String) -> Unit,
    onYearSelected: (Int) -> Unit,
    onPaperTypeSelected: (String) -> Unit,
    onViewPaperClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            SelectionDropdown(
                label = "Academic Level",
                options = academicLevelOptions,
                selectedLabel = academicLevelOptions.firstOrNull { it.value == selectedAcademicLevel }?.label
                    ?: selectedAcademicLevel,
                enabled = academicLevelOptions.isNotEmpty(),
                onSelected = { option -> onAcademicLevelSelected(option.value) }
            )

            SelectionDropdown(
                label = "Subject",
                options = subjectOptions,
                selectedLabel = subjectOptions.firstOrNull { it.value == selectedSubjectCode }?.label.orEmpty(),
                enabled = selectedAcademicLevel.isNotBlank() && subjectOptions.isNotEmpty(),
                onSelected = { option -> onSubjectSelected(option.value) }
            )

            SelectionDropdown(
                label = "Board",
                options = boardOptions,
                selectedLabel = boardOptions.firstOrNull { it.value == selectedBoardName }?.label.orEmpty(),
                enabled = selectedSubjectCode?.isNotBlank() == true && boardOptions.isNotEmpty(),
                onSelected = { option -> onBoardSelected(option.value) }
            )

            SelectionDropdown(
                label = "Year",
                options = yearOptions,
                selectedLabel = yearOptions.firstOrNull { it.value == selectedYear }?.label.orEmpty(),
                enabled = selectedBoardName?.isNotBlank() == true && yearOptions.isNotEmpty(),
                onSelected = { option -> onYearSelected(option.value) }
            )

            SelectionDropdown(
                label = "Paper Type",
                options = paperTypeOptions,
                selectedLabel = paperTypeOptions.firstOrNull { it.value == selectedPaperType }?.label.orEmpty(),
                enabled = selectedYear != null && paperTypeOptions.isNotEmpty(),
                onSelected = { option -> onPaperTypeSelected(option.value) }
            )

            QArenaPrimaryButton(
                text = if (isLoading) "Loading..." else "View Paper",
                onClick = onViewPaperClick,
                enabled = canViewPaper && !isLoading
            )
        }
    }
}

@Composable
private fun <T> SelectionDropdown(
    label: String,
    options: List<FilterOption<T>>,
    selectedLabel: String,
    enabled: Boolean,
    onSelected: (FilterOption<T>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val canOpen = enabled && options.isNotEmpty()

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = canOpen) { expanded = !expanded },
            label = { Text(text = label) },
            trailingIcon = {
                Text(
                    text = if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.labelMedium
                )
            },
            singleLine = true
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = canOpen) { expanded = !expanded }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.label) },
                    onClick = {
                        Log.d("BoardPapers", "selected $label value=${option.value} label=${option.label}")
                        expanded = false
                        onSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun GroupHeader(title: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun groupLoadedPapers(
    loadedPapers: List<com.qarena.android.data.remote.dto.BoardPaperDetailResponse>
): List<Pair<String, List<com.qarena.android.data.remote.dto.BoardPaperDetailResponse>>> {
    val order = listOf("CQ", "MCQ", "WRITTEN")
    val grouped = loadedPapers.groupBy {
        it.exam?.paperType?.trim()?.uppercase().orEmpty().ifBlank { "OTHER" }
    }

    return order.mapNotNull { paperType ->
        grouped[paperType]?.let { paperType to it }
    } + grouped.keys
        .filter { it !in order }
        .sorted()
        .mapNotNull { paperType -> grouped[paperType]?.let { paperType to it } }
}