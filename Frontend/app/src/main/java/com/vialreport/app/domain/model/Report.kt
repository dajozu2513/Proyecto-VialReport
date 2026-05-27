package com.vialreport.app.domain.model

data class ReportPhoto(val id: String, val url: String, val uploadedAt: String)

data class StatusLogEntry(
    val id: String,
    val oldStatus: String,
    val newStatus: String,
    val note: String?,
    val changedAt: String
)

data class Report(
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    val status: String,
    val priority: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val citizenName: String,
    val createdAt: String,
    val updatedAt: String,
    val photos: List<ReportPhoto> = emptyList(),
    val statusLog: List<StatusLogEntry> = emptyList()
)
