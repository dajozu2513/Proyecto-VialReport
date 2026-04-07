package com.vialreport.app.domain.model

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
    val updatedAt: String
)
