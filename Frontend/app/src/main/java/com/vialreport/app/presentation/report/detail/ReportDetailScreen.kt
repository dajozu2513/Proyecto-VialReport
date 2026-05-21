package com.vialreport.app.presentation.report.detail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vialreport.app.domain.model.Report
import com.vialreport.app.domain.model.ReportPhoto
import com.vialreport.app.presentation.report.util.priorityColor
import com.vialreport.app.presentation.report.util.priorityLabel
import com.vialreport.app.presentation.report.util.statusColor
import com.vialreport.app.presentation.report.util.statusLabel
import com.vialreport.app.presentation.report.util.typeLabel

private const val BASE_URL = "https://proyecto-vialreport-8it4.onrender.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: ReportDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val report = state.report
    val context = LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val mimeType = context.contentResolver.getType(it) ?: "image/jpeg"
            val bytes = context.contentResolver.openInputStream(it)?.readBytes() ?: return@let
            viewModel.uploadPhoto(bytes, mimeType)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del reporte") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (report != null) {
                        IconButton(onClick = { onEdit(report.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
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

                report != null -> {
                    ReportDetailContent(report = report)

                    HorizontalDivider()

                    PhotosSection(
                        photos          = report.photos,
                        isUploading     = state.isUploadingPhoto,
                        photoError      = state.photoError,
                        onAddPhoto      = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onClearError    = viewModel::clearPhotoError
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotosSection(
    photos: List<ReportPhoto>,
    isUploading: Boolean,
    photoError: String?,
    onAddPhoto: () -> Unit,
    onClearError: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Fotos (${photos.size})", style = MaterialTheme.typography.titleSmall)

            Button(
                onClick = onAddPhoto,
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                }
                Text(
                    text = if (isUploading) "  Validando..." else "  Agregar foto",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        photoError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (photos.isEmpty() && !isUploading) {
            Text(
                text = "Sin fotos adjuntas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                photos.forEach { photo ->
                    AsyncImage(
                        model = "$BASE_URL${photo.url}",
                        contentDescription = "Foto del reporte",
                        modifier = Modifier
                            .width(160.dp)
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportDetailContent(report: Report) {
    Text(text = report.title, style = MaterialTheme.typography.headlineSmall)

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
            label = { Text(priorityLabel(report.priority)) },
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = priorityColor(report.priority).copy(alpha = 0.15f),
                labelColor = priorityColor(report.priority)
            )
        )
        SuggestionChip(
            onClick = {},
            label = { Text(typeLabel(report.type)) }
        )
    }

    HorizontalDivider()

    DetailRow(label = "Descripción", value = report.description)
    DetailRow(label = "Ciudadano", value = report.citizenName)
    DetailRow(label = "Dirección", value = report.address)
    DetailRow(label = "Coordenadas", value = "${report.latitude}, ${report.longitude}")

    HorizontalDivider()

    DetailRow(label = "Creado", value = report.createdAt)
    DetailRow(label = "Actualizado", value = report.updatedAt)
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
