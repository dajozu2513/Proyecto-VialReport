package com.vialreport.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class HeatmapPoint(
    val latitude:  Double,
    val longitude: Double,
    val weight:    Int,
    val status:    String,
    val typeId:    String
)

@Serializable
data class MapReportPoint(
    val id:        String,
    val latitude:  Double,
    val longitude: Double,
    val status:    String,
    val typeName:  String,
    val zone:      String?
)
