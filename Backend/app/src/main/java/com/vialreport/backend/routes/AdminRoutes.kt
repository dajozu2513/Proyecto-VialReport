package com.vialreport.backend.routes

import com.vialreport.backend.dto.ApiResponse
import com.vialreport.backend.service.AdminService
import com.vialreport.backend.util.UserRole
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRoutes(adminService: AdminService) {

    route("/admin") {

        authenticate {

            // GET /admin/stats — solo admin
            get("/stats") {
                val principal = call.principal<JWTPrincipal>()!!
                val role      = principal.getClaim("role", String::class)!!

                if (role != UserRole.ADMIN) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ApiResponse<Unit>(success = false, message = "Acceso denegado: se requiere rol admin")
                    )
                    return@get
                }

                try {
                    val stats = adminService.getStats()
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(success = true, message = "OK", data = stats)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Unit>(success = false, message = e.message ?: "Error interno")
                    )
                }
            }
        }
    }
}
