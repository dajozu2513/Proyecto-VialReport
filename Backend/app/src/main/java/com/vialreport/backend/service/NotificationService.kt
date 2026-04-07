package com.vialreport.backend.service

import com.vialreport.backend.dto.NotificationResponse
import com.vialreport.backend.repository.NotificationRepository
import org.jetbrains.exposed.sql.transactions.transaction

class NotificationService(
    private val notificationRepository: NotificationRepository
) {

    fun getByUser(userId: Int): List<NotificationResponse> = transaction {
        notificationRepository.findByUser(userId).map { it.toResponse() }
    }

    fun getUnreadByUser(userId: Int): List<NotificationResponse> = transaction {
        notificationRepository.findUnreadByUser(userId).map { it.toResponse() }
    }

    fun markAsRead(notificationId: Int): Boolean = transaction {
        notificationRepository.markAsRead(notificationId)
    }

    fun markAllAsRead(userId: Int): Boolean = transaction {
        notificationRepository.markAllAsRead(userId)
    }

    // Llamado internamente cuando cambia el estado de un reporte
    fun notifyStatusChange(
        userId: Int,
        reportId: Int,
        oldStatus: String,
        newStatus: String
    ) {
        val statusLabels = mapOf(
            "new"         to "Recibido",
            "verified"    to "Verificado",
            "in_progress" to "En Proceso",
            "repairing"   to "En Reparación",
            "resolved"    to "Resuelto",
            "rejected"    to "Rechazado",
            "duplicate"   to "Duplicado"
        )

        val label = statusLabels[newStatus] ?: newStatus

        notificationRepository.create(
            userId   = userId,
            reportId = reportId,
            title    = "Tu reporte fue actualizado",
            body     = "El estado cambió de ${statusLabels[oldStatus]} a $label"
        )
    }
}