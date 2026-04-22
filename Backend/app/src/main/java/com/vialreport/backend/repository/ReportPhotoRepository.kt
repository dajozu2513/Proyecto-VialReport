package com.vialreport.backend.repository

import com.vialreport.backend.model.ReportPhoto
import com.vialreport.backend.model.ReportPhotos
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ReportPhotoRepository {

    private fun rowToPhoto(row: ResultRow) = ReportPhoto(
        id         = row[ReportPhotos.id].value,
        reportId   = row[ReportPhotos.reportId],
        url        = row[ReportPhotos.url],
        uploadedAt = row[ReportPhotos.uploadedAt]
    )

    fun findByReport(reportId: Int): List<ReportPhoto> = transaction {
        ReportPhotos.select { ReportPhotos.reportId eq reportId }.map { rowToPhoto(it) }
    }

    fun create(reportId: Int, url: String): ReportPhoto = transaction {
        val id = ReportPhotos.insertAndGetId {
            it[ReportPhotos.reportId] = reportId
            it[ReportPhotos.url]      = url
        }
        ReportPhotos.select { ReportPhotos.id eq id }.map { rowToPhoto(it) }.single()
    }
}
