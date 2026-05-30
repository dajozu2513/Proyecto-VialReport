package com.vialreport.app.presentation.report.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vialreport.app.domain.usecase.report.CreateReportUseCase
import com.vialreport.app.domain.usecase.report.GetIncidentTypesUseCase
import com.vialreport.app.domain.usecase.report.GetReportByIdUseCase
import com.vialreport.app.domain.usecase.report.UpdateReportUseCase
import com.vialreport.app.domain.usecase.report.UploadPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportFormViewModel @Inject constructor(
    private val createReportUseCase: CreateReportUseCase,
    private val updateReportUseCase: UpdateReportUseCase,
    private val getReportByIdUseCase: GetReportByIdUseCase,
    private val getIncidentTypesUseCase: GetIncidentTypesUseCase,
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Bytes de la foto seleccionada (no se almacenan en UiState para evitar memory issues)
    private var pendingPhotoBytes: ByteArray? = null
    private var pendingPhotoMime: String = "image/jpeg"

    private val reportId: String? = savedStateHandle.get<String>("id")?.takeIf { it.isNotEmpty() }

    private val _uiState = MutableStateFlow(
        ReportFormUiState(isLoading = reportId != null, isEdit = reportId != null)
    )
    val uiState: StateFlow<ReportFormUiState> = _uiState.asStateFlow()

    init {
        loadIncidentTypes()
        if (reportId != null) loadReport()
    }

    // ── Field updates ────────────────────────────────────────
    fun onTitleChange(v: String)       = _uiState.update { it.copy(title = v).recompute() }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v).recompute() }
    fun onTypeIdChange(v: String)      = _uiState.update { it.copy(typeId = v).recompute() }
    fun onAddressChange(v: String)     = _uiState.update { it.copy(address = v).recompute() }

    // ── Location ─────────────────────────────────────────────
    // ── Foto pendiente ────────────────────────────────────────
    fun onPhotoSelected(bytes: ByteArray, mime: String) {
        pendingPhotoBytes = bytes
        pendingPhotoMime  = mime
        _uiState.update { it.copy(hasPendingPhoto = true) }
    }

    fun onPhotoPendingRemoved() {
        pendingPhotoBytes = null
        _uiState.update { it.copy(hasPendingPhoto = false) }
    }

    // ── Location ─────────────────────────────────────────────
    fun onLocationFetching() = _uiState.update {
        it.copy(locationStatus = LocationStatus.FETCHING, locationError = null)
    }

    fun onLocationObtained(lat: Double, lng: Double) = _uiState.update {
        it.copy(
            latitude       = lat,
            longitude      = lng,
            locationStatus = LocationStatus.LOCATED,
            locationError  = null
        ).recompute()
    }

    fun onAddressObtained(address: String) {
        // Only auto-fill if the user hasn't typed anything yet
        if (_uiState.value.address.isBlank()) {
            _uiState.update { it.copy(address = address).recompute() }
        }
    }

    fun onLocationError(msg: String) = _uiState.update {
        it.copy(locationStatus = LocationStatus.ERROR, locationError = msg)
    }

    // ── Load & Save ───────────────────────────────────────────
    private fun loadIncidentTypes() {
        viewModelScope.launch {
            runCatching { getIncidentTypesUseCase() }
                .onSuccess { types ->
                    _uiState.update { state ->
                        val selectedType = if (state.typeId.isBlank()) types.firstOrNull()?.id ?: "" else state.typeId
                        state.copy(incidentTypes = types, typeId = selectedType).recompute()
                    }
                }
                .onFailure { _uiState.update { it.copy(error = "Error al cargar tipos de incidente") } }
        }
    }

    private fun loadReport() {
        viewModelScope.launch {
            runCatching { getReportByIdUseCase(reportId ?: return@launch) }
                .onSuccess { report ->
                    if (report != null) {
                        _uiState.update {
                            it.copy(
                                isLoading      = false,
                                title          = report.title,
                                description    = report.description,
                                address        = report.address,
                                latitude       = report.latitude,
                                longitude      = report.longitude,
                                locationStatus = LocationStatus.LOCATED,
                                status         = report.status
                            ).recompute()
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Reporte no encontrado") }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar") }
                }
        }
    }

    fun save(onDone: () -> Unit) {
        val state = _uiState.value
        if (!state.canSave) return
        val lat = state.latitude ?: return
        val lng = state.longitude ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null).recompute() }
            runCatching {
                if (reportId == null) {
                    createReportUseCase(state.typeId, state.title.trim(), state.description.trim(), lat, lng, state.address.trim())
                } else {
                    updateReportUseCase(reportId, state.typeId, state.title.trim(), state.description.trim(), lat, lng, state.address.trim())
                }
            }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message ?: "Error al guardar").recompute() }
                    return@launch
                }
                .onSuccess { savedReport ->
                    _uiState.update { it.copy(isSaving = false) }
                    // Subir foto pendiente si la hay (solo en creación, o en edición también)
                    val bytes = pendingPhotoBytes
                    if (bytes != null) {
                        _uiState.update { it.copy(isUploadingPhoto = true) }
                        runCatching { uploadPhotoUseCase(savedReport.id, bytes, pendingPhotoMime) }
                            .onFailure { e ->
                                // El reporte ya se guardó; la foto falló pero no bloqueamos
                                _uiState.update { it.copy(isUploadingPhoto = false, error = "Reporte guardado, pero la foto falló: ${e.message}") }
                            }
                        pendingPhotoBytes = null
                        _uiState.update { it.copy(isUploadingPhoto = false, hasPendingPhoto = false) }
                    }
                    onDone()
                }
        }
    }
}
