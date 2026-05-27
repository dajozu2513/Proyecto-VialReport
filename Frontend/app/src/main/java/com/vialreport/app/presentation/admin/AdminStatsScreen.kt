package com.vialreport.app.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vialreport.app.data.remote.dto.AdminStatsDto
import com.vialreport.app.presentation.report.util.statusColor
import com.vialreport.app.presentation.report.util.statusLabel
import com.vialreport.app.presentation.report.util.typeLabel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatsScreen(
    onBack: () -> Unit,
    viewModel: AdminStatsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::loadStats) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                state.stats != null -> {
                    StatsContent(stats = state.stats!!)
                }
            }
        }
    }
}

@Composable
private fun StatsContent(stats: AdminStatsDto) {
    // ── Summary cards ─────────────────────────────────────────
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryCard(
            label = "Total reportes",
            value = stats.totalReports.toString(),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Hoy",
            value = stats.todayReports.toString(),
            modifier = Modifier.weight(1f)
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryCard(
            label = "Resueltos hoy",
            value = stats.resolvedToday.toString(),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Prom. resolución",
            value = "${stats.avgResolutionHours.roundToInt()}h",
            modifier = Modifier.weight(1f)
        )
    }

    // ── Por estado ────────────────────────────────────────────
    if (stats.byStatus.isNotEmpty()) {
        SectionCard(title = "Por estado") {
            val total = stats.byStatus.values.sum().coerceAtLeast(1)
            stats.byStatus.entries.sortedByDescending { it.value }.forEach { (status, count) ->
                BreakdownRow(
                    label    = statusLabel(status),
                    count    = count,
                    total    = total,
                    barColor = statusColor(status)
                )
            }
        }
    }

    // ── Por tipo ──────────────────────────────────────────────
    if (stats.byType.isNotEmpty()) {
        SectionCard(title = "Por tipo de incidente") {
            val total = stats.byType.values.sum().coerceAtLeast(1)
            stats.byType.entries.sortedByDescending { it.value }.forEach { (type, count) ->
                BreakdownRow(
                    label    = typeLabel(type),
                    count    = count,
                    total    = total,
                    barColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // ── Por zona ──────────────────────────────────────────────
    if (stats.byZone.isNotEmpty()) {
        SectionCard(title = "Por zona") {
            val total = stats.byZone.values.sum().coerceAtLeast(1)
            stats.byZone.entries.sortedByDescending { it.value }.forEach { (zone, count) ->
                BreakdownRow(
                    label    = zone.ifBlank { "Sin zona" },
                    count    = count,
                    total    = total,
                    barColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun BreakdownRow(label: String, count: Int, total: Int, barColor: Color) {
    val fraction = count.toFloat() / total.toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "$count (${(fraction * 100).roundToInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth(),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.15f)
        )
    }
}
