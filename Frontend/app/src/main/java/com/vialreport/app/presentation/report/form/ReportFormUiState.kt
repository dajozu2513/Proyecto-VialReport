package com.vialreport.app.presentation.report.form

import com.vialreport.app.domain.model.IncidentType

enum class LocationStatus { IDLE, FETCHING, LOCATED, ERROR }

data class ReportFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEdit: Boolean = false,
    val title: String = "",
    val description: String = "",
    val typeId: String = "",
    val status: String = "new",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationStatus: LocationStatus = LocationStatus.IDLE,
    val locationError: String? = null,
    val incidentTypes: List<IncidentType> = emptyList(),
    // Foto pendiente (solo en modo creación)
    val hasPendingPhoto: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val canSave: Boolean = false,
    val error: String? = null
) {
    fun recompute() = copy(
        canSave = title.isNotBlank() && description.isNotBlank() &&
                  address.isNotBlank() && latitude != null && longitude != null &&
                  typeId.isNotBlank() && !isLoading && !isSaving
    )
}
