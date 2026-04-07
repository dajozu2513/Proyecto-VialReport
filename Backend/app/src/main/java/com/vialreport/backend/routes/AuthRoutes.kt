package com.vialreport.backend.routes

import com.vialreport.backend.dto.ApiResponse
import com.vialreport.backend.dto.LoginRequest
import com.vialreport.backend.dto.RegisterRequest
import com.vialreport.backend.service.AuthService
import com.vialreport.backend.util.ConflictException
import com.vialreport.backend.util.UnauthorizedException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {

    route("/auth") {

        // POST /auth/register
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val result  = authService.register(request)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(success = true, message = "Usuario registrado", data = result)
                )
            } catch (e: ConflictException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ApiResponse<Unit>(success = false, message = e.message ?: "Conflicto")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Unit>(success = false, message = e.message ?: "Error interno")
                )
            }
        }

        // POST /auth/login
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                val result  = authService.login(request)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, message = "Login exitoso", data = result)
                )
            } catch (e: UnauthorizedException) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<Unit>(success = false, message = e.message ?: "No autorizado")
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