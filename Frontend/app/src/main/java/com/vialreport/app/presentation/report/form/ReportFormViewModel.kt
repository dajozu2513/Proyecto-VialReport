package com.vialreport.app.presentation.report.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vialreport.app.domain.usecase.report.CreateReportUseCase
import com.vialreport.app.domain.usecase.report.GetIncidentTypesUseCase
import com.vialreport.app.domain.usecase.report.GetReportByIdUseCase
import com.vialreport.app.domain.usecase.report.UpdateReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportFormViewModel @Inject constructor(
    private val createReportUseCase: CreateReportUseCase,
    private val updateReportUseCase: UpdateReportUseCase,
    private val getReportByIdUseCase: GetReportByIdUseCase,
    private val getIncidentTypesUseCase: GetIncidentTypesUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reportId: String? = savedStateHandle.get<String>("id")?.takeIf { it.isNotEmpty() }

    private val title       = MutableStateFlow(savedStateHandle["title"] ?: "")
    private val description = MutableStateFlow(savedStateHandle["description"] ?: "")
    private val typeId      = MutableStateFlow(savedStateHandle["typeId"] ?: "")
    private val status      = MutableStateFlow(savedStateHandle["status"] ?: "new")
    private val address     = MutableStateFlow(savedStateHandle["address"] ?: "")
    private val latitude    = MutableStateFlow(savedStateHandle["latitude"] ?: "")
    private val longitude   = MutableStateFlow(savedStateHandle["longitude"] ?: "")
    private val isLoading   = MutableStateFlow(false)
    private val isSaving    = MutableStateFlow(false)
    private val error       = MutableStateFlow<String?>(null)
    private val incidentTypes = MutableStateFlow(emptyList<com.vialreport.app.domain.model.IncidentType>())

    val uiState: StateFlow<ReportFormUiState> = combine(
        combine(title, description, typeId, status, address) { t, d, ty, s, a -> listOf(t, d, ty, s, a) },
        combine(latitude, longitude, isLoading, isSaving, error) { lat, lng, load, save, err ->
            listOf(lat, lng, load.toString(), save.toString(), err ?: "")
        },
        incidentTypes
    ) { first, second, types ->
        val t    = first[0]; val d  = first[1]; val ty = first[2]
        val s    = first[3]; val a  = first[4]
        val lat  = second[0]; val lng = second[1]
        val load = second[2].toBoolean(); val save = second[3].toBoolean()
        val err  = second[4].takeIf { it.isNotEmpty() }

        val canSave = t.isNotBlank() && d.isNotBlank() && a.isNotBlank() &&
                lat.isNotBlank() && lng.isNotBlank() && ty.isNotBlank() &&
                !load && !save

        ReportFormUiState(
            isLoading     = load,
            isSaving      = save,
            isEdit        = reportId != null,
            title         = t,
            description   = d,
            typeId        = ty,
            status        = s,
            address       = a,
            latitude      = lat,
            longitude     = lng,
            incidentTypes = types,
            canSave       = canSave,
            error         = err
        )
    }.stateIn(
        scope   = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportFormUiState()
    )

    init {
        loadIncidentTypes()
        if (reportId != null) loadReport()
    }

    private fun loadIncidentTypes() {
        viewModelScope.launch {
            runCatching { getIncidentTypesUseCase() }
                .onSuccess { types ->
                    incidentTypes.value = types
                    if (typeId.value.isBlank() && types.isNotEmpty()) {
                        typeId.value = types.first().id
                    }
                }
                .onFailure { error.value = "Error al cargar tipos de incidente" }
        }
    }

    private fun loadReport() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            runCatching { getReportByIdUseCase(reportId ?: return@launch) }
                .onSuccess { report ->
                    if (report != null) {
                        if (title.value.isBlank()) title.value = report.title
                        if (description.value.isBlank()) description.value = report.description
                        if (address.value.isBlank()) address.value = report.address
                        if (latitude.value.isBlank()) latitude.value = report.latitude.toString()
                        if (longitude.value.isBlank()) longitude.value = report.longitude.toString()
                        status.value = report.status
                    } else {
                        error.value = "Reporte no encontrado"
                    }
                }
                .onFailure { e -> error.value = e.message ?: "Error al cargar el reporte" }
            isLoading.value = false
        }
    }

    fun onTitleChange(v: String)       { title.value = v;       savedStateHandle["title"] = v }
    fun onDescriptionChange(v: String) { description.value = v; savedStateHandle["description"] = v }
    fun onTypeIdChange(v: String)      { typeId.value = v;      savedStateHandle["typeId"] = v }
    fun onStatusChange(v: String)      { status.value = v;      savedStateHandle["status"] = v }
    fun onAddressChange(v: String)     { address.value = v;     savedStateHandle["address"] = v }
    fun onLatitudeChange(v: String)    { latitude.value = v;    savedStateHandle["latitude"] = v }
    fun onLongitudeChange(v: String)   { longitude.value = v;   savedStateHandle["longitude"] = v }

    fun save(onDone: () -> Unit) {
        if (!uiState.value.canSave) return
        viewModelScope.launch {
            isSaving.value = true
            error.value = null
            runCatching {
                val lat = latitude.value.toDoubleOrNull() ?: 0.0
                val lng = longitude.value.toDoubleOrNull() ?: 0.0
                if (reportId == null) {
                    createReportUseCase(typeId.value, title.value.trim(), description.value.trim(), lat, lng, address.value.trim())
                } else {
                    updateReportUseCase(reportId, status.value)
                }
            }
                .onSuccess { onDone() }
                .onFailure { e -> error.value = e.message ?: "Error al guardar" }
            isSaving.value = false
        }
    }
}
