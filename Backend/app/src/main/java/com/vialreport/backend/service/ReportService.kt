package com.vialreport.backend.service

import com.vialreport.backend.dto.ReportRequest
import com.vialreport.backend.dto.ReportResponse
import com.vialreport.backend.dto.UpdateStatusRequest
import com.vialreport.backend.repository.*
import com.vialreport.backend.util.BadRequestException
import com.vialreport.backend.util.NotFoundException
import com.vialreport.backend.util.ReportStatus
import com.vialreport.backend.util.UserRole
import org.jetbrains.exposed.sql.transactions.transaction

class ReportService(
    private val reportRepository: ReportRepository,
    private val userRepository: UserRepository,
    private val incidentTypeRepository: IncidentTypeRepository,
    private val crewRepository: CrewRepository,
    private val photoRepository: ReportPhotoRepository,
    private val statusLogRepository: ReportStatusLogRepository,
    private val notificationService: NotificationService
) {

    fun getAll(): List<ReportResponse> = transaction {
        reportRepository.findAll().map { buildResponse(it.id) }
    }

    fun getById(id: Int): ReportResponse = transaction {
        reportRepository.findById(id)
            ?: throw NotFoundException("Reporte #$id no encontrado")
        buildResponse(id)
    }

    fun getByCitizen(citizenId: Int): List<ReportResponse> = transaction {
        reportRepository.findByCitizen(citizenId).map { buildResponse(it.id) }
    }

    fun getByStatus(status: String): List<ReportResponse> = transaction {
        if (!ReportStatus.isValid(status)) throw BadRequestException("Estado inválido: $status")
        reportRepository.findByStatus(status).map { buildResponse(it.id) }
    }

    fun create(citizenId: Int, request: ReportRequest): ReportResponse = transaction {
        // Validar que el tipo de incidente existe
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

        buildResponse(report.id)
    }

    fun updateStatus(
        reportId: Int,
        adminId: Int,
        request: UpdateStatusRequest
    ): ReportResponse = transaction {
        // Validar estado
        if (!ReportStatus.isValid(request.status)) {
            throw BadRequestException("Estado inválido: ${request.status}")
        }

        // Obtener reporte actual
        val report = reportRepository.findById(reportId)
            ?: throw NotFoundException("Reporte #$reportId no encontrado")

        // Guardar en el historial
        statusLogRepository.create(
            reportId  = reportId,
            changedBy = adminId,
            oldStatus = report.status,
            newStatus = request.status,
            note      = request.note
        )

        // Actualizar el reporte
        reportRepository.updateStatus(reportId, request.status, request.crewId)

        // Notificar al ciudadano
        notificationService.notifyStatusChange(
            userId    = report.citizenId,
            reportId  = reportId,
            oldStatus = report.status,
            newStatus = request.status
        )

        buildResponse(reportId)
    }

    fun delete(reportId: Int, requesterId: Int, requesterRole: String): Boolean = transaction {
        val report = reportRepository.findById(reportId)
            ?: throw NotFoundException("Reporte #$reportId no encontrado")

        // Solo el dueño o un admin pueden eliminar
        if (requesterRole != UserRole.ADMIN && report.citizenId != requesterId) {
            throw com.vialreport.backend.util.UnauthorizedException("No tenés permiso para eliminar este reporte")
        }

        reportRepository.delete(reportId)
    }

    // Construye el ReportResponse completo con todos los datos relacionados
    private fun buildResponse(reportId: Int): ReportResponse {
        val report   = reportRepository.findById(reportId)!!
        val citizen  = userRepository.findById(report.citizenId)!!
        val type     = incidentTypeRepository.findById(report.typeId)!!
        val crew     = report.crewId?.let { crewRepository.findById(it) }
        val photos   = photoRepository.findByReport(reportId)
        val logs     = statusLogRepository.findByReport(reportId)

        return ReportResponse(
            id          = report.id,
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
                val changedBy = userRepository.findById(log.changedBy)!!
                com.vialreport.backend.dto.StatusLogResponse(
                    id        = log.id,
                    changedBy = changedBy.toResponse(),
                    oldStatus = log.oldStatus,
                    newStatus = log.newStatus,
                    note      = log.note,
                    changedAt = log.changedAt.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
            },
            createdAt   = report.createdAt.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            updatedAt   = report.updatedAt.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }
}