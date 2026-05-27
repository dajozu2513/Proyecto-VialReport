package com.vialreport.app.data.remote.dto

data class AdminStatsDto(
    val totalReports: Int,
    val byStatus: Map<String, Int>,
    val byType: Map<String, Int>,
    val byZone: Map<String, Int>,
    val todayReports: Int,
    val resolvedToday: Int,
    val avgResolutionHours: Double
)
