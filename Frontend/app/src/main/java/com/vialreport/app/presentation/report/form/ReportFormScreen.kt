package com.vialreport.app.presentation.report.form

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.vialreport.app.domain.model.IncidentType
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.resume
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ReportFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // ── Permission launcher ───────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            scope.launch { fetchLocation(context, fusedLocationClient, viewModel) }
        } else {
            viewModel.onLocationError("Permiso de ubicación denegado. Actívalo en Ajustes.")
        }
    }

    fun requestLocation() {
        val hasFine   = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            scope.launch { fetchLocation(context, fusedLocationClient, viewModel) }
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEdit) "Editar reporte" else "Nuevo reporte") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Título *") },
                singleLine = true
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descripción *") },
                minLines = 3
            )

            // ── Ubicación ─────────────────────────────────────
            LocationSection(
                status    = state.locationStatus,
                latitude  = state.latitude,
                longitude = state.longitude,
                error     = state.locationError,
                isEdit    = state.isEdit,
                onRequest = ::requestLocation
            )

            OutlinedTextField(
                value = state.address,
                onValueChange = viewModel::onAddressChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Dirección *") },
                singleLine = true,
                supportingText = {
                    if (state.locationStatus == LocationStatus.LOCATED && state.address.isBlank()) {
                        Text("Puedes editar la dirección manualmente", style = MaterialTheme.typography.labelSmall)
                    }
                }
            )

            if (state.incidentTypes.isNotEmpty()) {
                IncidentTypeDropdown(
                    selected   = state.typeId,
                    options    = state.incidentTypes,
                    onSelected = viewModel::onTypeIdChange
                )
            }

            Button(
                onClick = { viewModel.save(onSaved) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSave
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                    Text("  Guardando...")
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}

@Composable
private fun LocationSection(
    status: LocationStatus,
    latitude: Double?,
    longitude: Double?,
    error: String?,
    isEdit: Boolean,
    onRequest: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Ubicación del incidente *", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            when (status) {
                LocationStatus.IDLE -> {
                    Button(onClick = onRequest, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Usar mi ubicación actual")
                    }
                }

                LocationStatus.FETCHING -> {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Obteniendo ubicación...", style = MaterialTheme.typography.bodySmall)
                    }
                }

                LocationStatus.LOCATED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(
                                    text = "%.5f, %.5f".format(latitude, longitude),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEdit) "Ubicación del reporte" else "Ubicación obtenida",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = onRequest,
                            contentPadding = ButtonDefaults.TextButtonContentPadding
                        ) {
                            Text("Actualizar", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                LocationStatus.ERROR -> {
                    Text(
                        text = error ?: "Error al obtener ubicación",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(onClick = onRequest, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Reintentar")
                    }
                }
            }
        }
    }
}

// ── Location helpers (suspend, called from coroutineScope) ────

private suspend fun fetchLocation(
    context: android.content.Context,
    client: com.google.android.gms.location.FusedLocationProviderClient,
    viewModel: ReportFormViewModel
) {
    viewModel.onLocationFetching()
    try {
        val location = suspendCancellableCoroutine { continuation ->
            val cts = com.google.android.gms.tasks.CancellationTokenSource()
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(null) }
            continuation.invokeOnCancellation { cts.cancel() }
        }
        if (location != null) {
            viewModel.onLocationObtained(location.latitude, location.longitude)
            reverseGeocode(context, location.latitude, location.longitude)?.let { address ->
                viewModel.onAddressObtained(address)
            }
        } else {
            viewModel.onLocationError("No se pudo obtener la ubicación. Asegúrate de tener el GPS activo.")
        }
    } catch (e: SecurityException) {
        viewModel.onLocationError("Permiso de ubicación denegado.")
    } catch (e: Exception) {
        viewModel.onLocationError("Error: ${e.message}")
    }
}

private suspend fun reverseGeocode(
    context: android.content.Context,
    lat: Double,
    lng: Double
): String? = withContext(Dispatchers.IO) {
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(lat, lng, 1) { addresses ->
                    cont.resume(addresses.firstOrNull())
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()
        }
        listOfNotNull(
            address?.thoroughfare,
            address?.subLocality ?: address?.locality,
            address?.adminArea
        ).joinToString(", ").takeIf { it.isNotBlank() }
    } catch (e: Exception) {
        null // Geocoder falla silenciosamente; el usuario ingresa dirección manualmente
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncidentTypeDropdown(
    selected: String,
    options: List<IncidentType>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.find { it.id == selected }?.let { "${it.icon} ${it.name}" } ?: ""

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            label = { Text("Tipo de incidente *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { type ->
                DropdownMenuItem(
                    text = { Text("${type.icon} ${type.name}") },
                    onClick = { onSelected(type.id); expanded = false }
                )
            }
        }
    }
}
