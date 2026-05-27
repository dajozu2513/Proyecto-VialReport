package com.vialreport.app.data.remote.dto

data class MapPointDto(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val typeName: String,
    val zone: String?
)
