package com.vialreport.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

// Respuestas de auth
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val cedula: String? = null,
    val role: String = "citizen"
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse
)

// Respuesta de usuario (sin password ni cedula)
@Serializable
data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null,
    val isVerified: Boolean,
    val createdAt: String
)

@Serializable
data class UpdateProfileRequest(
    val name: String,
    val phone: String? = null
)