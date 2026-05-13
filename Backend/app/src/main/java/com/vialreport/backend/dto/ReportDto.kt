package com.vialreport.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReportRequest(
    val typeId: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val address: String
)

@Serializable
data class ReportResponse(
    val id: String,
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

@Serializable
data class UpdateStatusRequest(
    val status: String,
    val note: String? = null,
    val crewId: String? = null
)

@Serializable
data class PhotoResponse(
    val id: String,
    val url: String,
    val uploadedAt: String
)

@Serializable
data class PhotoUploadResponse(
    val id: String,
    val url: String,
    val uploadedAt: String,
    val aiApproved: Boolean
)

@Serializable
data class StatusLogResponse(
    val id: String,
    val oldStatus: String,
    val newStatus: String,
    val note: String? = null,
    val changedAt: String
)
