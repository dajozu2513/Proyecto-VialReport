package com.vialreport.backend.model

import com.vialreport.backend.dto.PhotoResponse
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ReportPhotos : IntIdTable("report_photos") {
    val reportId   = integer("report_id").references(Reports.id)
    val url        = varchar("url", 500)
    val uploadedAt = datetime("uploaded_at").default(LocalDateTime.now())
}

data class ReportPhoto(
    val id: Int,
    val reportId: Int,
    val url: String,
    val uploadedAt: LocalDateTime
) {
    fun toResponse() = PhotoResponse(
        id         = id,
        url        = url,
        uploadedAt = uploadedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}