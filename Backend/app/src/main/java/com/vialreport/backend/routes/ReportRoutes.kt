package com.vialreport.backend.routes

import com.vialreport.backend.dto.ApiResponse
import com.vialreport.backend.dto.ReportRequest
import com.vialreport.backend.dto.UpdateStatusRequest
import com.vialreport.backend.service.PhotoService
import com.vialreport.backend.service.ReportService
import com.vialreport.backend.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reportRoutes(reportService: ReportService, photoService: PhotoService) {

    route("/reports") {

        // GET /reports — admin ve todos, ciudadano ve los suyos
        authenticate {
            get {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val role      = principal.getClaim("role", String::class)!!
                    val userId    = principal.getClaim("userId", Int::class)!!

                    val reports = if (role == UserRole.ADMIN) {
                        val status = call.request.queryParameters["status"]
                        val typeId = call.request.queryParameters["typeId"]?.toIntOrNull()
                        val zone   = call.request.queryParameters["zone"]
                        reportService.getFiltered(status, typeId, zone)
                    } else {
                        reportService.getByCitizen(userId)
                    }

                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(success = true, message = "OK", data = reports)
                    )
                } catch (e: BadRequestException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(success = false, message = e.message ?: "Bad request")
                    )
                }
            }

            // GET /reports/{id}
            get("/{id}") {
                try {
                    val id     = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("ID inválido")
                    val report = reportService.getById(id)
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(success = true, message = "OK", data = report)
                    )
                } catch (e: NotFoundException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No encontrado")
                    )
                } catch (e: BadRequestException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(success = false, message = e.message ?: "Bad request")
                    )
                }
            }

            // POST /reports — ciudadano crea un reporte
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId    = principal.getClaim("userId", Int::class)!!
                    val request   = call.receive<ReportRequest>()
                    val report    = reportService.create(userId, request)
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(success = true, message = "Reporte creado", data = report)
                    )
                } catch (e: NotFoundException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No encontrado")
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Unit>(success = false, message = e.message ?: "Error interno")
                    )
                }
            }

            // PUT /reports/{id}/status — solo admin
            put("/{id}/status") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val role      = principal.getClaim("role", String::class)!!
                    val adminId   = principal.getClaim("userId", Int::class)!!

                    if (role != UserRole.ADMIN) {
                        throw UnauthorizedException("Solo admins pueden cambiar el estado")
                    }

                    val id      = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("ID inválido")
                    val request = call.receive<UpdateStatusRequest>()
                    val report  = reportService.updateStatus(id, adminId, request)

                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(success = true, message = "Estado actualizado", data = report)
                    )
                } catch (e: UnauthorizedException) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No autorizado")
                    )
                } catch (e: NotFoundException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No encontrado")
                    )
                } catch (e: BadRequestException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(success = false, message = e.message ?: "Bad request")
                    )
                }
            }

            // DELETE /reports/{id}
            delete("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId    = principal.getClaim("userId", Int::class)!!
                    val role      = principal.getClaim("role", String::class)!!
                    val id        = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("ID inválido")

                    reportService.delete(id, userId, role)
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse<Unit>(success = true, message = "Reporte eliminado")
                    )
                } catch (e: UnauthorizedException) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No autorizado")
                    )
                } catch (e: NotFoundException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No encontrado")
                    )
                }
            }

            // POST /reports/{id}/photos — ciudadano dueño o admin
            post("/{id}/photos") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId    = principal.getClaim("userId", Int::class)!!
                    val role      = principal.getClaim("role", String::class)!!
                    val reportId  = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("ID inválido")
                    val multipart = call.receiveMultipart()
                    val photo     = photoService.uploadPhoto(reportId, userId, role, multipart)
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(success = true, message = "Foto subida", data = photo)
                    )
                } catch (e: BadRequestException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(success = false, message = e.message ?: "Bad request")
                    )
                } catch (e: NotFoundException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No encontrado")
                    )
                } catch (e: UnauthorizedException) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No autorizado")
                    )
                }
            }

            // GET /reports/{id}/photos — solo admin
            get("/{id}/photos") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val role      = principal.getClaim("role", String::class)!!
                    if (role != UserRole.ADMIN) throw UnauthorizedException("Solo admins pueden ver las fotos")
                    val reportId = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("ID inválido")
                    val photos   = photoService.getPhotos(reportId)
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(success = true, message = "OK", data = photos)
                    )
                } catch (e: UnauthorizedException) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No autorizado")
                    )
                } catch (e: NotFoundException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No encontrado")
                    )
                } catch (e: BadRequestException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(success = false, message = e.message ?: "Bad request")
                    )
                }
            }
        }
    }
}