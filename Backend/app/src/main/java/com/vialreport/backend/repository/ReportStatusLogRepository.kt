package com.vialreport.backend.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.vialreport.backend.model.ReportStatusLog
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

class ReportStatusLogRepository(db: MongoDatabase) {

    private val col = db.getCollection<Document>("report_status_logs")

    private fun Date.toLocalDateTime() =
        toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime()

    private fun LocalDateTime.toDate() =
        Date.from(atOffset(ZoneOffset.UTC).toInstant())

    private fun docToLog(doc: Document) = ReportStatusLog(
        id        = doc.getObjectId("_id"),
        reportId  = doc.getString("reportId"),
        changedBy = doc.getString("changedBy"),
        oldStatus = doc.getString("oldStatus"),
        newStatus = doc.getString("newStatus"),
        note      = doc.getString("note"),
        changedAt = (doc.getDate("changedAt") ?: Date()).toLocalDateTime()
    )

    suspend fun findByReport(reportId: String): List<ReportStatusLog> =
        col.find(Filters.eq("reportId", reportId))
            .sort(Sorts.descending("changedAt"))
            .toList().map { docToLog(it) }

    suspend fun create(
        reportId: String,
        changedBy: String,
        oldStatus: String,
        newStatus: String,
        note: String?
    ): ReportStatusLog {
        val log = ReportStatusLog(
            reportId  = reportId,
            changedBy = changedBy,
            oldStatus = oldStatus,
            newStatus = newStatus,
            note      = note
        )
        val doc = Document("_id", log.id)
            .append("reportId", log.reportId)
            .append("changedBy", log.changedBy)
            .append("oldStatus", log.oldStatus)
            .append("newStatus", log.newStatus)
            .append("note", log.note)
            .append("changedAt", log.changedAt.toDate())
        col.insertOne(doc)
        return log
    }
}
