package com.vialreport.backend.model

import com.vialreport.backend.dto.NotificationResponse
import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Notification(
    val id: ObjectId = ObjectId(),
    val userId: String,
    val reportId: String,
    val title: String,
    val body: String,
    val isRead: Boolean = false,
    val sentAt: LocalDateTime = LocalDateTime.now()
) {
    fun toResponse() = NotificationResponse(
        id       = id.toHexString(),
        reportId = reportId,
        title    = title,
        body     = body,
        isRead   = isRead,
        sentAt   = sentAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}
