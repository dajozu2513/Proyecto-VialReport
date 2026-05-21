package com.vialreport.app.presentation.report.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vialreport.app.domain.model.Report
import com.vialreport.app.presentation.report.util.priorityColor
import com.vialreport.app.presentation.report.util.statusColor
import com.vialreport.app.presentation.report.util.statusLabel
import com.vialreport.app.presentation.report.util.typeLabel

private val STATUS_FILTERS = listOf(
    "" to "Todos",
    "new" to "Nuevo",
    "in_progress" to "En progreso",
    "resolved" to "Resuelto",
    "rejected" to "Rechazado"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    onReportClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onLogout: () -> Unit,
    shouldRefresh: Boolean,
    viewModel: ReportListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) viewModel.loadReports()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VialReport") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo reporte")
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text("Buscar reporte") },
                singleLine = true
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                STATUS_FILTERS.forEach { (value, label) ->
                    FilterChip(
                        selected = state.selectedStatus == value,
                        onClick = { viewModel.onStatusFilterChange(value) },
                        label = { Text(label) }
                    )
                }
            }

            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                state.error != null -> {
                    Text(
                        text = state.error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                state.items.isEmpty() -> {
                    Text("No hay reportes registrados")
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(items = state.items, key = { it.id }) { report ->
                            ReportCard(
                                report = report,
                                onClick = { onReportClick(report.id) },
                                onEdit = { onEditClick(report.id) },
                                onDelete = { viewModel.deleteReport(report.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(
    report: Report,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = report.title, style = MaterialTheme.typography.titleMedium)

            Text(
                text = report.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(statusLabel(report.status)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor(report.status).copy(alpha = 0.15f),
                        labelColor = statusColor(report.status)
                    )
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text(typeLabel(report.type)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = priorityColor(report.priority).copy(alpha = 0.15f),
                        labelColor = priorityColor(report.priority)
                    )
                )
            }

            Text(
                text = "Ciudadano: ${report.citizenName}",
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}
