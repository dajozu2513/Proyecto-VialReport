package com.vialreport.backend.repository

import com.vialreport.backend.model.ReportStatusLog
import com.vialreport.backend.model.ReportStatusLogs
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ReportStatusLogRepository {

    private fun rowToLog(row: ResultRow) = ReportStatusLog(
        id        = row[ReportStatusLogs.id].value,
        reportId  = row[ReportStatusLogs.reportId],
        changedBy = row[ReportStatusLogs.changedBy],
        oldStatus = row[ReportStatusLogs.oldStatus],
        newStatus = row[ReportStatusLogs.newStatus],
        note      = row[ReportStatusLogs.note],
        changedAt = row[ReportStatusLogs.changedAt]
    )

    fun findByReport(reportId: Int): List<ReportStatusLog> = transaction {
        ReportStatusLogs.select { ReportStatusLogs.reportId eq reportId }
            .orderBy(ReportStatusLogs.changedAt, SortOrder.DESC)
            .map { rowToLog(it) }
    }

    fun create(
        reportId: Int,
        changedBy: Int,
        oldStatus: String,
        newStatus: String,
        note: String?
    ): ReportStatusLog = transaction {
        val id = ReportStatusLogs.insertAndGetId {
            it[ReportStatusLogs.reportId]  = reportId
            it[ReportStatusLogs.changedBy] = changedBy
            it[ReportStatusLogs.oldStatus] = oldStatus
            it[ReportStatusLogs.newStatus] = newStatus
            it[ReportStatusLogs.note]      = note
        }
        ReportStatusLogs.select { ReportStatusLogs.id eq id }.map { rowToLog(it) }.single()
    }
}
