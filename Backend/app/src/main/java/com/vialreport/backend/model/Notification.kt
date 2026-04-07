package com.vialreport.backend.model


import com.vialreport.backend.dto.NotificationResponse
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Notifications : IntIdTable("notifications") {
    val userId   = integer("user_id").references(Users.id)
    val reportId = integer("report_id").references(Reports.id)
    val title    = varchar("title", 120)
    val body     = text("body")
    val isRead   = bool("is_read").default(false)
    val sentAt   = datetime("sent_at").default(LocalDateTime.now())
}

data class Notification(
    val id: Int,
    val userId: Int,
    val reportId: Int,
    val title: String,
    val body: String,
    val isRead: Boolean,
    val sentAt: LocalDateTime
) {
    fun toResponse() = NotificationResponse(
        id       = id,
        reportId = reportId,
        title    = title,
        body     = body,
        isRead   = isRead,
        sentAt   = sentAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}