package com.vialreport.backend.model

import com.vialreport.backend.dto.StatusLogResponse
import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ReportStatusLog(
    val id: ObjectId = ObjectId(),
    val reportId: String,
    val changedBy: String,
    val oldStatus: String,
    val newStatus: String,
    val note: String? = null,
    val changedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toResponse() = StatusLogResponse(
        id        = id.toHexString(),
        oldStatus = oldStatus,
        newStatus = newStatus,
        note      = note,
        changedAt = changedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}
