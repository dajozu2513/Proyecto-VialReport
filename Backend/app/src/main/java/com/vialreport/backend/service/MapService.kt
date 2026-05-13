package com.vialreport.backend.service

import com.vialreport.backend.dto.HeatmapPoint
import com.vialreport.backend.dto.MapReportPoint
import com.vialreport.backend.repository.ReportRepository

class MapService(private val reportRepository: ReportRepository) {

    suspend fun getHeatmap(typeId: String?, zone: String?, status: String?): List<HeatmapPoint> =
        reportRepository.getHeatmapData(typeId, zone, status)

    suspend fun getMapPoints(typeId: String?, zone: String?): List<MapReportPoint> =
        reportRepository.getMapPoints(typeId, zone)
}
