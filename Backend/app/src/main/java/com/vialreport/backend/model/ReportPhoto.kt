package com.vialreport.backend.model

import com.vialreport.backend.dto.PhotoResponse
import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ReportPhoto(
    val id: ObjectId = ObjectId(),
    val reportId: String,
    val url: String,
    val uploadedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toResponse() = PhotoResponse(
        id         = id.toHexString(),
        url        = url,
        uploadedAt = uploadedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}
