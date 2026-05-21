package com.vialreport.app.data.remote.dto

data class CitizenDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)

data class IncidentTypeDto(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val defaultPriority: Int
)

data class PhotoDto(
    val id: String,
    val url: String,
    val uploadedAt: String,
    val aiApproved: Boolean = true
)

data class ReportDto(
    val id: String,
    val citizen: CitizenDto,
    val type: IncidentTypeDto,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val photos: List<PhotoDto>? = null,
    val createdAt: String?,
    val updatedAt: String?
)
