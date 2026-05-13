package com.vialreport.backend.service

import com.vialreport.backend.dto.AdminStatsResponse
import com.vialreport.backend.repository.ReportRepository

class AdminService(private val reportRepository: ReportRepository) {

    fun getStats(): AdminStatsResponse = reportRepository.getStats()
}
