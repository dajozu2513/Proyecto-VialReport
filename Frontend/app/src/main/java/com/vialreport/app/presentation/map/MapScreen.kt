package com.vialreport.app.presentation.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vialreport.app.data.remote.dto.MapPointDto
import com.vialreport.app.presentation.report.util.statusColor
import com.vialreport.app.presentation.report.util.statusLabel
import com.vialreport.app.presentation.report.util.typeLabel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val context = LocalContext.current

    LaunchedEffect(state.selectedPoint) {
        if (state.selectedPoint != null) {
            scaffoldState.bottomSheetState.expand()
        } else {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            state.selectedPoint?.let { point ->
                PointInfoSheet(point = point)
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mapa de reportes")
                        Text(
                            text = "${state.points.size} incidentes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::loadPoints) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(
                    text = state.error ?: "Error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                OsmMapView(
                    points = state.points,
                    onMarkerClick = { point ->
                        viewModel.selectPoint(if (state.selectedPoint?.id == point.id) null else point)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun OsmMapView(
    points: List<MapPointDto>,
    onMarkerClick: (MapPointDto) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
            // Default center — Bolivia (Cochabamba)
            controller.setCenter(GeoPoint(-17.3895, -66.1568))
            isTilesScaledToDpi = true
        }
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose { mapView.onPause() }
    }

    LaunchedEffect(points) {
        mapView.overlays.clear()
        if (points.isEmpty()) return@LaunchedEffect

        points.forEach { point ->
            val marker = Marker(mapView).apply {
                position  = GeoPoint(point.latitude, point.longitude)
                title     = typeLabel(point.typeName)
                snippet   = point.zone ?: ""
                icon      = createColoredCircle(context, statusColor(point.status).toArgb())
                setOnMarkerClickListener { _, _ ->
                    onMarkerClick(point)
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        // Auto-center on first point
        val first = points.first()
        mapView.controller.animateTo(GeoPoint(first.latitude, first.longitude))
        mapView.invalidate()
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}

private fun createColoredCircle(
    context: android.content.Context,
    argbColor: Int,
    size: Int = 48
): BitmapDrawable {
    val bitmap  = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas  = Canvas(bitmap)
    val fill    = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = argbColor }
    val border  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color       = android.graphics.Color.WHITE
        style       = Paint.Style.STROKE
        strokeWidth = 4f
    }
    val r = size / 2f - 3f
    canvas.drawCircle(size / 2f, size / 2f, r, fill)
    canvas.drawCircle(size / 2f, size / 2f, r, border)
    return BitmapDrawable(context.resources, bitmap)
}

@Composable
private fun PointInfoSheet(point: MapPointDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = typeLabel(point.typeName),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text(statusLabel(point.status)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor(point.status).copy(alpha = 0.15f),
                        labelColor = statusColor(point.status)
                    )
                )
            }
            if (!point.zone.isNullOrBlank()) {
                Text(
                    text = "Zona: ${point.zone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "%.5f, %.5f".format(point.latitude, point.longitude),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
