package com.vialreport.backend.model

import com.vialreport.backend.dto.StatusLogResponse
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ReportStatusLogs : IntIdTable("report_status_log") {
    val reportId  = integer("report_id").references(Reports.id)
    val changedBy = integer("changed_by").references(Users.id)
    val oldStatus = varchar("old_status", 30)
    val newStatus = varchar("new_status", 30)
    val note      = text("note").nullable()
    val changedAt = datetime("changed_at").default(LocalDateTime.now())
}

data class ReportStatusLog(
    val id: Int,
    val reportId: Int,
    val changedBy: Int,
    val oldStatus: String,
    val newStatus: String,
    val note: String?,
    val changedAt: LocalDateTime
)