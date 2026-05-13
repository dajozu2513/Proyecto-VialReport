package com.vialreport.backend.service

import com.vialreport.backend.dto.HeatmapPoint
import com.vialreport.backend.dto.MapReportPoint
import com.vialreport.backend.repository.ReportRepository

class MapService(private val reportRepository: ReportRepository) {

    fun getHeatmap(typeId: Int?, zone: String?, status: String?): List<HeatmapPoint> =
        reportRepository.getHeatmapData(typeId, zone, status)

    fun getMapPoints(typeId: Int?, zone: String?): List<MapReportPoint> =
        reportRepository.getMapPoints(typeId, zone)
}
