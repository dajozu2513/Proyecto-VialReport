package com.vialreport.backend.routes

import com.vialreport.backend.dto.ApiResponse
import com.vialreport.backend.dto.CrewRequest
import com.vialreport.backend.service.CrewService
import com.vialreport.backend.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.crewRoutes(crewService: CrewService) {

    authenticate {
        route("/crews") {

            // GET /crews
            get {
                val crews = crewService.getAll()
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, message = "OK", data = crews)
                )
            }

            // GET /crews/available
            get("/available") {
                val crews = crewService.getAvailable()
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, message = "OK", data = crews)
                )
            }

            // GET /crews/{id}
            get("/{id}") {
                try {
                    val id   = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("ID inválido")
                    val crew = crewService.getById(id)
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(success = true, message = "OK", data = crew)
                    )
                } catch (e: NotFoundException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No encontrada")
                    )
                }
            }

            // POST /crews — solo admin
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val role      = principal.getClaim("role", String::class)!!

                    if (role != UserRole.ADMIN) {
                        throw UnauthorizedException("Solo admins pueden crear cuadrillas")
                    }

                    val request = call.receive<CrewRequest>()
                    val crew    = crewService.create(request)
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(success = true, message = "Cuadrilla creada", data = crew)
                    )
                } catch (e: UnauthorizedException) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No autorizado")
                    )
                }
            }

            // PATCH /crews/{id}/availability — solo admin
            patch("/{id}/availability") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val role      = principal.getClaim("role", String::class)!!

                    if (role != UserRole.ADMIN) {
                        throw UnauthorizedException("Solo admins pueden modificar cuadrillas")
                    }

                    val id        = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("ID inválido")
                    val available = call.receive<Map<String, Boolean>>()["available"]
                        ?: throw BadRequestException("Campo 'available' requerido")
                    val crew      = crewService.setAvailability(id, available)

                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(success = true, message = "Disponibilidad actualizada", data = crew)
                    )
                } catch (e: UnauthorizedException) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No autorizado")
                    )
                } catch (e: NotFoundException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No encontrada")
                    )
                }
            }

            // DELETE /crews/{id} — solo admin
            delete("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val role      = principal.getClaim("role", String::class)!!

                    if (role != UserRole.ADMIN) {
                        throw UnauthorizedException("Solo admins pueden eliminar cuadrillas")
                    }

                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("ID inválido")
                    crewService.delete(id)
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse<Unit>(success = true, message = "Cuadrilla eliminada")
                    )
                } catch (e: UnauthorizedException) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No autorizado")
                    )
                } catch (e: NotFoundException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No encontrada")
                    )
                }
            }
        }
    }
}