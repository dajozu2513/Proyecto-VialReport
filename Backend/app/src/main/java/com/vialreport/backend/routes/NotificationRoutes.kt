package com.vialreport.backend.routes

import com.vialreport.backend.dto.ApiResponse
import com.vialreport.backend.service.NotificationService
import com.vialreport.backend.util.BadRequestException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.notificationRoutes(notificationService: NotificationService) {

    authenticate {
        route("/notifications") {

            // GET /notifications — notificaciones del usuario autenticado
            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId    = principal.getClaim("userId", Int::class)!!
                val notifs    = notificationService.getByUser(userId)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, message = "OK", data = notifs)
                )
            }

            // GET /notifications/unread
            get("/unread") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId    = principal.getClaim("userId", Int::class)!!
                val notifs    = notificationService.getUnreadByUser(userId)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, message = "OK", data = notifs)
                )
            }

            // PATCH /notifications/{id}/read
            patch("/{id}/read") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("ID inválido")
                    notificationService.markAsRead(id)
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse<Unit>(success = true, message = "Marcada como leída")
                    )
                } catch (e: BadRequestException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(success = false, message = e.message ?: "Bad request")
                    )
                }
            }

            // PATCH /notifications/read-all
            patch("/read-all") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId    = principal.getClaim("userId", Int::class)!!
                notificationService.markAllAsRead(userId)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse<Unit>(success = true, message = "Todas marcadas como leídas")
                )
            }
        }
    }
}