package com.vialreport.app.presentation.report.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vialreport.app.domain.usecase.report.CreateReportUseCase
import com.vialreport.app.domain.usecase.report.GetReportByIdUseCase
import com.vialreport.app.domain.usecase.report.UpdateReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportFormViewModel @Inject constructor(
    private val createReportUseCase: CreateReportUseCase,
    private val updateReportUseCase: UpdateReportUseCase,
    private val getReportByIdUseCase: GetReportByIdUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_DESC = "description"
        private const val KEY_TYPE = "type"
        private const val KEY_STATUS = "status"
        private const val KEY_PRIORITY = "priority"
        private const val KEY_ADDRESS = "address"
        private const val KEY_LAT = "latitude"
        private const val KEY_LNG = "longitude"
        private const val KEY_CITIZEN = "citizenName"
    }

    private val reportId: String? = savedStateHandle.get<String>(KEY_ID)?.takeIf { it.isNotEmpty() }

    private val title = MutableStateFlow(savedStateHandle[KEY_TITLE] ?: "")
    private val description = MutableStateFlow(savedStateHandle[KEY_DESC] ?: "")
    private val type = MutableStateFlow(savedStateHandle[KEY_TYPE] ?: "pothole")
    private val status = MutableStateFlow(savedStateHandle[KEY_STATUS] ?: "new")
    private val priority = MutableStateFlow(savedStateHandle[KEY_PRIORITY] ?: "medium")
    private val address = MutableStateFlow(savedStateHandle[KEY_ADDRESS] ?: "")
    private val latitude = MutableStateFlow(savedStateHandle[KEY_LAT] ?: "")
    private val longitude = MutableStateFlow(savedStateHandle[KEY_LNG] ?: "")
    private val citizenName = MutableStateFlow(savedStateHandle[KEY_CITIZEN] ?: "")
    private val isLoading = MutableStateFlow(false)
    private val isSaving = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ReportFormUiState> = combine(
        combine(title, description, type, status, priority) { t, d, ty, s, p ->
            listOf(t, d, ty, s, p)
        },
        combine(address, latitude, longitude, citizenName, isLoading) { a, lat, lng, c, load ->
            listOf(a, lat, lng, c, load.toString())
        },
        isSaving,
        error
    ) { firstGroup, secondGroup, saving, err ->
        val t = firstGroup[0]
        val d = firstGroup[1]
        val ty = firstGroup[2]
        val s = firstGroup[3]
        val p = firstGroup[4]
        val a = secondGroup[0]
        val lat = secondGroup[1]
        val lng = secondGroup[2]
        val c = secondGroup[3]
        val loading = secondGroup[4].toBoolean()

        val canSave = t.isNotBlank() && d.isNotBlank() && a.isNotBlank() &&
                c.isNotBlank() && lat.isNotBlank() && lng.isNotBlank() &&
                !loading && !saving

        ReportFormUiState(
            isLoading = loading,
            isSaving = saving,
            isEdit = reportId != null,
            title = t,
            description = d,
            type = ty,
            status = s,
            priority = p,
            address = a,
            latitude = lat,
            longitude = lng,
            citizenName = c,
            canSave = canSave,
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportFormUiState()
    )

    init {
        if (reportId != null) loadReport()
    }

    private fun loadReport() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            try {
                val report = getReportByIdUseCase(reportId ?: return@launch)
                if (report != null) {
                    if (title.value.isBlank()) { title.value = report.title; savedStateHandle[KEY_TITLE] = report.title }
                    if (description.value.isBlank()) { description.value = report.description; savedStateHandle[KEY_DESC] = report.description }
                    if (address.value.isBlank()) { address.value = report.address; savedStateHandle[KEY_ADDRESS] = report.address }
                    if (citizenName.value.isBlank()) { citizenName.value = report.citizenName; savedStateHandle[KEY_CITIZEN] = report.citizenName }
                    if (latitude.value.isBlank()) { latitude.value = report.latitude.toString(); savedStateHandle[KEY_LAT] = report.latitude.toString() }
                    if (longitude.value.isBlank()) { longitude.value = report.longitude.toString(); savedStateHandle[KEY_LNG] = report.longitude.toString() }
                    type.value = report.type; savedStateHandle[KEY_TYPE] = report.type
                    status.value = report.status; savedStateHandle[KEY_STATUS] = report.status
                    priority.value = report.priority; savedStateHandle[KEY_PRIORITY] = report.priority
                } else {
                    error.value = "Reporte no encontrado"
                }
            } catch (e: Exception) {
                error.value = e.message ?: "Error al cargar el reporte"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun onTitleChange(v: String) { savedStateHandle[KEY_TITLE] = v; title.value = v }
    fun onDescriptionChange(v: String) { savedStateHandle[KEY_DESC] = v; description.value = v }
    fun onTypeChange(v: String) { savedStateHandle[KEY_TYPE] = v; type.value = v }
    fun onStatusChange(v: String) { savedStateHandle[KEY_STATUS] = v; status.value = v }
    fun onPriorityChange(v: String) { savedStateHandle[KEY_PRIORITY] = v; priority.value = v }
    fun onAddressChange(v: String) { savedStateHandle[KEY_ADDRESS] = v; address.value = v }
    fun onLatitudeChange(v: String) { savedStateHandle[KEY_LAT] = v; latitude.value = v }
    fun onLongitudeChange(v: String) { savedStateHandle[KEY_LNG] = v; longitude.value = v }
    fun onCitizenNameChange(v: String) { savedStateHandle[KEY_CITIZEN] = v; citizenName.value = v }

    fun save(onDone: () -> Unit) {
        if (!uiState.value.canSave) return
        viewModelScope.launch {
            isSaving.value = true
            error.value = null
            try {
                val lat = latitude.value.toDoubleOrNull() ?: 0.0
                val lng = longitude.value.toDoubleOrNull() ?: 0.0

                if (reportId == null) {
                    createReportUseCase(
                        title = title.value.trim(),
                        description = description.value.trim(),
                        type = type.value,
                        status = status.value,
                        priority = priority.value,
                        address = address.value.trim(),
                        latitude = lat,
                        longitude = lng,
                        citizenName = citizenName.value.trim()
                    )
                } else {
                    updateReportUseCase(
                        id = reportId,
                        title = title.value.trim(),
                        description = description.value.trim(),
                        type = type.value,
                        status = status.value,
                        priority = priority.value,
                        address = address.value.trim(),
                        latitude = lat,
                        longitude = lng,
                        citizenName = citizenName.value.trim()
                    )
                }
                onDone()
            } catch (e: Exception) {
                error.value = e.message ?: "Error al guardar el reporte"
            } finally {
                isSaving.value = false
            }
        }
    }
}
