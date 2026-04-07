package com.vialreport.app.data.repository

import com.vialreport.app.data.remote.api.ReportApi
import com.vialreport.app.data.remote.dto.ReportRequestDto
import com.vialreport.app.data.remote.mapper.toDomain
import com.vialreport.app.domain.model.Report
import com.vialreport.app.domain.repository.IReportRepository
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val api: ReportApi
) : IReportRepository {

    override suspend fun getAll(): List<Report> {
        return api.getReports().map { it.toDomain() }
    }

    override suspend fun getById(id: String): Report? {
        return try {
            api.getReportById(id).toDomain()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun create(
        title: String,
        description: String,
        type: String,
        status: String,
        priority: String,
        address: String,
        latitude: Double,
        longitude: Double,
        citizenName: String
    ): Report {
        val now = java.time.Instant.now().toString()
        val request = ReportRequestDto(
            title = title,
            description = description,
            type = type,
            status = status,
            priority = priority,
            address = address,
            latitude = latitude,
            longitude = longitude,
            citizenName = citizenName,
            updatedAt = now
        )
        return api.createReport(request).toDomain()
    }

    override suspend fun update(
        id: String,
        title: String,
        description: String,
        type: String,
        status: String,
        priority: String,
        address: String,
        latitude: Double,
        longitude: Double,
        citizenName: String
    ): Report {
        val now = java.time.Instant.now().toString()
        val request = ReportRequestDto(
            title = title,
            description = description,
            type = type,
            status = status,
            priority = priority,
            address = address,
            latitude = latitude,
            longitude = longitude,
            citizenName = citizenName,
            updatedAt = now
        )
        return api.updateReport(id, request).toDomain()
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            api.deleteReport(id)
            true
        } catch (_: Exception) {
            false
        }
    }
}
