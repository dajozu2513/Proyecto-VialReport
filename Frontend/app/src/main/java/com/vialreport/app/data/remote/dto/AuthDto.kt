package com.vialreport.app.data.remote.dto

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val role: String = "citizen"
)

data class UpdateProfileRequestDto(
    val name: String,
    val phone: String? = null
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null
)

data class AuthResponseDto(
    val token: String,
    val user: UserDto
)
