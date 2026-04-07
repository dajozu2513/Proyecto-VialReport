package com.vialreport.backend.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.vialreport.backend.dto.AuthResponse
import com.vialreport.backend.dto.LoginRequest
import com.vialreport.backend.dto.RegisterRequest
import com.vialreport.backend.repository.UserRepository
import com.vialreport.backend.util.ConflictException
import com.vialreport.backend.util.UnauthorizedException
import org.mindrot.jbcrypt.BCrypt
import java.util.Date

class AuthService(
    private val userRepository: UserRepository,
    private val jwtSecret: String,
    private val jwtIssuer: String,
    private val jwtAudience: String
) {

    fun register(request: RegisterRequest): AuthResponse {
        // Verificar si el email ya existe
        if (userRepository.existsByEmail(request.email)) {
            throw ConflictException("El email ya está registrado")
        }

        // Hashear el password
        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())

        // Crear usuario
        val user = userRepository.create(
            name         = request.name,
            email        = request.email,
            passwordHash = passwordHash,
            role         = request.role,
            phone        = request.phone
        )

        // Generar token
        val token = generateToken(user.id, user.email, user.role)

        return AuthResponse(
            token = token,
            user  = user.toResponse()
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        // Buscar usuario por email
        val user = userRepository.findByEmail(request.email)
            ?: throw UnauthorizedException("Email o contraseña incorrectos")

        // Verificar password
        if (!BCrypt.checkpw(request.password, user.passwordHash)) {
            throw UnauthorizedException("Email o contraseña incorrectos")
        }

        // Generar token
        val token = generateToken(user.id, user.email, user.role)

        return AuthResponse(
            token = token,
            user  = user.toResponse()
        )
    }

    private fun generateToken(userId: Int, email: String, role: String): String {
        return JWT.create()
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000)) // 24h
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}