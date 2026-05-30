package com.vialreport.app.data.repository

import com.vialreport.app.data.remote.api.ReportApi
import com.vialreport.app.data.remote.dto.ReportRequestDto
import com.vialreport.app.data.remote.dto.UpdateStatusRequestDto
import com.vialreport.app.data.remote.mapper.toDomain
import com.vialreport.app.domain.model.IncidentType
import com.vialreport.app.domain.model.Report
import com.vialreport.app.domain.model.ReportPhoto
import com.vialreport.app.domain.repository.IReportRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val api: ReportApi
) : IReportRepository {

    override suspend fun getAll(): List<Report> =
        api.getReports().data?.map { it.toDomain() } ?: emptyList()

    override suspend fun getById(id: String): Report? =
        runCatching { api.getReportById(id).data?.toDomain() }.getOrNull()

    override suspend fun getIncidentTypes(): List<IncidentType> =
        api.getIncidentTypes().data?.map {
            IncidentType(it.id, it.name, it.icon, it.color)
        } ?: emptyList()

    override suspend fun create(
        typeId: String,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        address: String
    ): Report {
        val request = ReportRequestDto(typeId, title, description, latitude, longitude, address)
        return api.createReport(request).data!!.toDomain()
    }

    override suspend fun update(
        id: String,
        typeId: String,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        address: String
    ): Report {
        val request = ReportRequestDto(typeId, title, description, latitude, longitude, address)
        return api.updateReport(id, request).data!!.toDomain()
    }

    override suspend fun updateStatus(id: String, status: String): Report {
        val request = UpdateStatusRequestDto(status)
        return api.updateStatus(id, request).data!!.toDomain()
    }

    override suspend fun delete(id: String): Boolean =
        runCatching { api.deleteReport(id); true }.getOrDefault(false)

    override suspend fun uploadPhoto(reportId: String, imageBytes: ByteArray, mimeType: String): ReportPhoto {
        val ext = when (mimeType) { "image/png" -> "png"; "image/webp" -> "webp"; else -> "jpg" }
        val body = imageBytes.toRequestBody(mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData("photo", "photo.$ext", body)
        val data = api.uploadPhoto(reportId, part).data ?: error("Error al subir foto")
        return ReportPhoto(data.id, data.url, data.uploadedAt)
    }

    override suspend fun deletePhoto(reportId: String, photoId: String) {
        api.deletePhoto(reportId, photoId)
    }
}
