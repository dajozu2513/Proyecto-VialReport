package com.vialreport.backend.dto

import kotlinx.serialization.Serializable

// Lo que manda el ciudadano al crear un reporte
@Serializable
data class ReportRequest(
    val typeId: Int,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val address: String
)

// Lo que devuelve el servidor
@Serializable
data class ReportResponse(
    val id: Int,
    val citizen: UserResponse,
    val type: IncidentTypeResponse,
    val crew: CrewResponse? = null,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val photos: List<PhotoResponse> = emptyList(),
    val statusLog: List<StatusLogResponse> = emptyList(),
    val createdAt: String,
    val updatedAt: String
)

// Para actualizar estado (solo admin)
@Serializable
data class UpdateStatusRequest(
    val status: String,
    val note: String? = null,
    val crewId: Int? = null
)

@Serializable
data class PhotoResponse(
    val id: Int,
    val url: String,
    val uploadedAt: String
)

@Serializable
data class StatusLogResponse(
    val id: Int,
    val changedBy: UserResponse,
    val oldStatus: String,
    val newStatus: String,
    val note: String? = null,
    val changedAt: String
)