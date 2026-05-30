package com.vialreport.app.domain.repository

import com.vialreport.app.domain.model.IncidentType
import com.vialreport.app.domain.model.Report
import com.vialreport.app.domain.model.ReportPhoto

interface IReportRepository {

    suspend fun getAll(): List<Report>

    suspend fun getById(id: String): Report?

    suspend fun getIncidentTypes(): List<IncidentType>

    suspend fun create(
        typeId: String,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        address: String
    ): Report

    suspend fun update(
        id: String,
        typeId: String,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        address: String
    ): Report

    suspend fun updateStatus(id: String, status: String): Report

    suspend fun delete(id: String): Boolean

    suspend fun uploadPhoto(reportId: String, imageBytes: ByteArray, mimeType: String): ReportPhoto

    suspend fun deletePhoto(reportId: String, photoId: String)
}
