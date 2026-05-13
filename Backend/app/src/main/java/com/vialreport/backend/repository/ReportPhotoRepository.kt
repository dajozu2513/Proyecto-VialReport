package com.vialreport.backend.repository

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.vialreport.backend.model.ReportPhoto
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

class ReportPhotoRepository(db: MongoDatabase) {

    private val col = db.getCollection<Document>("report_photos")

    private fun Date.toLocalDateTime() =
        toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime()

    private fun LocalDateTime.toDate() =
        Date.from(atOffset(ZoneOffset.UTC).toInstant())

    private fun docToPhoto(doc: Document) = ReportPhoto(
        id         = doc.getObjectId("_id"),
        reportId   = doc.getString("reportId"),
        url        = doc.getString("url"),
        uploadedAt = (doc.getDate("uploadedAt") ?: Date()).toLocalDateTime()
    )

    suspend fun findByReport(reportId: String): List<ReportPhoto> =
        col.find(Filters.eq("reportId", reportId)).toList().map { docToPhoto(it) }

    suspend fun create(reportId: String, url: String): ReportPhoto {
        val photo = ReportPhoto(reportId = reportId, url = url)
        val doc = Document("_id", photo.id)
            .append("reportId", photo.reportId)
            .append("url", photo.url)
            .append("uploadedAt", photo.uploadedAt.toDate())
        col.insertOne(doc)
        return photo
    }
}
