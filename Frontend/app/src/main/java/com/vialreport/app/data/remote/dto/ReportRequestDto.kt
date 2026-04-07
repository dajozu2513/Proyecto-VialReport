package com.vialreport.app.data.remote.dto

data class ReportRequestDto(
    val title: String,
    val description: String,
    val type: String,
    val status: String,
    val priority: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val citizenName: String,
    val updatedAt: String
)
