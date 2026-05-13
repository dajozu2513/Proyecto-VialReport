package com.vialreport.backend.service

import com.vialreport.backend.dto.PhotoResponse
import com.vialreport.backend.dto.PhotoUploadResponse
import com.vialreport.backend.repository.ReportPhotoRepository
import com.vialreport.backend.repository.ReportRepository
import com.vialreport.backend.util.BadRequestException
import com.vialreport.backend.util.NotFoundException
import com.vialreport.backend.util.UnauthorizedException
import com.vialreport.backend.util.UserRole
import io.ktor.http.content.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class PhotoService(
    private val reportRepository: ReportRepository,
    private val photoRepository: ReportPhotoRepository,
    private val photoAiService: PhotoAiService,
    private val uploadDir: String
) {

    private val allowedMimeTypes = setOf("image/jpeg", "image/png", "image/webp")

    fun getPhotos(reportId: Int): List<PhotoResponse> {
        reportRepository.findById(reportId)
            ?: throw NotFoundException("Reporte #$reportId no encontrado")
        return photoRepository.findByReport(reportId).map { it.toResponse() }
    }

    suspend fun uploadPhoto(
        reportId: Int,
        requesterId: Int,
        requesterRole: String,
        multipart: MultiPartData
    ): PhotoUploadResponse {
        val report = reportRepository.findById(reportId)
            ?: throw NotFoundException("Reporte #$reportId no encontrado")

        if (requesterRole != UserRole.ADMIN && report.citizenId != requesterId) {
            throw UnauthorizedException("No tenés permiso para subir fotos a este reporte")
        }

        var fileBytes: ByteArray? = null
        var mimeType = "image/jpeg"
        var originalName = "photo.jpg"

        multipart.forEachPart { part ->
            if (part is PartData.FileItem) {
                mimeType = part.contentType?.toString() ?: "image/jpeg"
                originalName = part.originalFileName ?: "photo.jpg"
                fileBytes = part.streamProvider().readBytes()
            }
            part.dispose()
        }

        val bytes = fileBytes ?: throw BadRequestException("No se recibió ningún archivo")
        if (bytes.isEmpty()) throw BadRequestException("El archivo está vacío")
        if (mimeType !in allowedMimeTypes) throw BadRequestException("Tipo de archivo no permitido. Use JPEG, PNG o WebP")
        if (bytes.size > 10 * 1024 * 1024) throw BadRequestException("La imagen no puede superar 10 MB")

        val approved = photoAiService.isRoadIncident(bytes, mimeType)
        if (!approved) throw BadRequestException("La imagen no parece mostrar un incidente vial real. Por favor suba una foto del problema en la vía pública.")

        val ext = when (mimeType) {
            "image/png"  -> "png"
            "image/webp" -> "webp"
            else         -> "jpg"
        }
        val fileName = "${UUID.randomUUID()}.$ext"
        val dir = File(uploadDir)
        if (!dir.exists()) dir.mkdirs()
        File(dir, fileName).writeBytes(bytes)

        val url = "/uploads/$fileName"
        val photo = photoRepository.create(reportId, url)

        return PhotoUploadResponse(
            id          = photo.id,
            url         = photo.url,
            uploadedAt  = photo.uploadedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            aiApproved  = true
        )
    }
}
