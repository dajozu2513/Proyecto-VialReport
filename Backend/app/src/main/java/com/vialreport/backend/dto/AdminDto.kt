package com.vialreport.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class AdminStatsResponse(
    val totalReports:       Int,
    val byStatus:           Map<String, Int>,
    val byType:             Map<String, Int>,
    val byZone:             Map<String, Int>,
    val todayReports:       Int,
    val resolvedToday:      Int,
    val avgResolutionHours: Double
)
