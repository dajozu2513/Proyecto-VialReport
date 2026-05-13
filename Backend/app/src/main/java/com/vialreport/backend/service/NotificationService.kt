package com.vialreport.backend.service

import com.vialreport.backend.dto.NotificationResponse
import com.vialreport.backend.repository.NotificationRepository

class NotificationService(
    private val notificationRepository: NotificationRepository
) {

    suspend fun getByUser(userId: String): List<NotificationResponse> =
        notificationRepository.findByUser(userId).map { it.toResponse() }

    suspend fun getUnreadByUser(userId: String): List<NotificationResponse> =
        notificationRepository.findUnreadByUser(userId).map { it.toResponse() }

    suspend fun markAsRead(notificationId: String): Boolean =
        notificationRepository.markAsRead(notificationId)

    suspend fun markAllAsRead(userId: String): Boolean =
        notificationRepository.markAllAsRead(userId)

    suspend fun notifyStatusChange(
        userId: String,
        reportId: String,
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
