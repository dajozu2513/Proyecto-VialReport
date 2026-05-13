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

    suspend fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw ConflictException("El email ya está registrado")
        }
        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())
        val user = userRepository.create(
            name         = request.name,
            email        = request.email,
            passwordHash = passwordHash,
            role         = request.role,
            phone        = request.phone,
            cedula       = request.cedula
        )
        return AuthResponse(token = generateToken(user.id.toHexString(), user.email, user.role),
                            user  = user.toResponse())
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw UnauthorizedException("Email o contraseña incorrectos")
        if (!BCrypt.checkpw(request.password, user.passwordHash)) {
            throw UnauthorizedException("Email o contraseña incorrectos")
        }
        return AuthResponse(token = generateToken(user.id.toHexString(), user.email, user.role),
                            user  = user.toResponse())
    }

    private fun generateToken(userId: String, email: String, role: String): String =
        JWT.create()
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000))
            .sign(Algorithm.HMAC256(jwtSecret))
}
