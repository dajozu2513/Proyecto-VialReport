package com.vialreport.backend.routes

import com.vialreport.backend.dto.ApiResponse
import com.vialreport.backend.dto.LoginRequest
import com.vialreport.backend.dto.RegisterRequest
import com.vialreport.backend.repository.UserRepository
import com.vialreport.backend.service.AuthService
import com.vialreport.backend.util.ConflictException
import com.vialreport.backend.util.NotFoundException
import com.vialreport.backend.util.UnauthorizedException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService, userRepository: UserRepository) {

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

        // GET /auth/me — perfil del usuario autenticado
        authenticate {
            get("/me") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId    = principal.getClaim("userId", Int::class)!!
                    val user      = userRepository.findById(userId)
                        ?: throw NotFoundException("Usuario no encontrado")
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(success = true, message = "OK", data = user.toResponse())
                    )
                } catch (e: NotFoundException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(success = false, message = e.message ?: "No encontrado")
                    )
                }
            }
        }
    }
}