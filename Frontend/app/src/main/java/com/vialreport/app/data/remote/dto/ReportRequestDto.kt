package com.vialreport.app.data.remote.dto

data class ReportRequestDto(
    val typeId: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val address: String
)
