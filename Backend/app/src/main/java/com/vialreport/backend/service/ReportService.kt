package com.vialreport.backend.service

import com.vialreport.backend.dto.ReportRequest
import com.vialreport.backend.dto.ReportResponse
import com.vialreport.backend.dto.StatusLogResponse
import com.vialreport.backend.dto.UpdateStatusRequest
import com.vialreport.backend.repository.*
import com.vialreport.backend.util.BadRequestException
import com.vialreport.backend.util.NotFoundException
import com.vialreport.backend.util.ReportStatus
import com.vialreport.backend.util.UnauthorizedException
import com.vialreport.backend.util.UserRole
import java.time.format.DateTimeFormatter

class ReportService(
    private val reportRepository: ReportRepository,
    private val userRepository: UserRepository,
    private val incidentTypeRepository: IncidentTypeRepository,
    private val crewRepository: CrewRepository,
    private val photoRepository: ReportPhotoRepository,
    private val statusLogRepository: ReportStatusLogRepository,
    private val notificationService: NotificationService
) {

    suspend fun getFiltered(status: String?, typeId: String?, zone: String?): List<ReportResponse> {
        if (status != null && !ReportStatus.isValid(status)) throw BadRequestException("Estado inválido: $status")
        return reportRepository.findFiltered(status, typeId, zone).map { buildResponse(it.id.toHexString()) }
    }

    suspend fun getById(id: String): ReportResponse {
        reportRepository.findById(id) ?: throw NotFoundException("Reporte $id no encontrado")
        return buildResponse(id)
    }

    suspend fun getByCitizen(citizenId: String): List<ReportResponse> =
        reportRepository.findByCitizen(citizenId).map { buildResponse(it.id.toHexString()) }

    suspend fun create(citizenId: String, request: ReportRequest): ReportResponse {
        incidentTypeRepository.findById(request.typeId)
            ?: throw NotFoundException("Tipo de incidente no encontrado")

        val report = reportRepository.create(
            citizenId   = citizenId,
            typeId      = request.typeId,
            title       = request.title,
            description = request.description,
            priority    = "medium",
            latitude    = request.latitude,
            longitude   = request.longitude,
            address     = request.address
        )
        return buildResponse(report.id.toHexString())
    }

    suspend fun update(
        reportId: String,
        requesterId: String,
        requesterRole: String,
        request: ReportRequest
    ): ReportResponse {
        val report = reportRepository.findById(reportId)
            ?: throw NotFoundException("Reporte $reportId no encontrado")
        if (requesterRole != UserRole.ADMIN && report.citizenId != requesterId) {
            throw UnauthorizedException("No tenés permiso para editar este reporte")
        }
        incidentTypeRepository.findById(request.typeId)
            ?: throw NotFoundException("Tipo de incidente no encontrado")
        reportRepository.update(reportId, request.typeId, request.title, request.description,
            request.latitude, request.longitude, request.address)
        return buildResponse(reportId)
    }

    suspend fun updateStatus(
        reportId: String,
        adminId: String,
        request: UpdateStatusRequest
    ): ReportResponse {
        if (!ReportStatus.isValid(request.status)) {
            throw BadRequestException("Estado inválido: ${request.status}")
        }
        val report = reportRepository.findById(reportId)
            ?: throw NotFoundException("Reporte $reportId no encontrado")

        statusLogRepository.create(
            reportId  = reportId,
            changedBy = adminId,
            oldStatus = report.status,
            newStatus = request.status,
            note      = request.note
        )
        reportRepository.updateStatus(reportId, request.status, request.crewId)
        notificationService.notifyStatusChange(
            userId    = report.citizenId,
            reportId  = reportId,
            oldStatus = report.status,
            newStatus = request.status
        )
        return buildResponse(reportId)
    }

    suspend fun delete(reportId: String, requesterId: String, requesterRole: String): Boolean {
        val report = reportRepository.findById(reportId)
            ?: throw NotFoundException("Reporte $reportId no encontrado")
        if (requesterRole != UserRole.ADMIN && report.citizenId != requesterId) {
            throw UnauthorizedException("No tenés permiso para eliminar este reporte")
        }
        return reportRepository.delete(reportId)
    }

    private suspend fun buildResponse(reportId: String): ReportResponse {
        val report  = reportRepository.findById(reportId)!!
        val citizen = userRepository.findById(report.citizenId)!!
        val type    = incidentTypeRepository.findById(report.typeId)!!
        val crew    = report.crewId?.let { crewRepository.findById(it) }
        val photos  = photoRepository.findByReport(reportId)
        val logs    = statusLogRepository.findByReport(reportId)
        val fmt     = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        return ReportResponse(
            id          = report.id.toHexString(),
            citizen     = citizen.toResponse(),
            type        = type.toResponse(),
            crew        = crew?.toResponse(),
            title       = report.title,
            description = report.description,
            status      = report.status,
            priority    = report.priority,
            latitude    = report.latitude,
            longitude   = report.longitude,
            address     = report.address,
            photos      = photos.map { it.toResponse() },
            statusLog   = logs.map { log ->
                StatusLogResponse(
                    id        = log.id.toHexString(),
                    oldStatus = log.oldStatus,
                    newStatus = log.newStatus,
                    note      = log.note,
                    changedAt = log.changedAt.format(fmt)
                )
            },
            createdAt   = report.createdAt.format(fmt),
            updatedAt   = report.updatedAt.format(fmt)
        )
    }
}
