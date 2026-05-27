package com.vialreport.app.domain.repository

interface IAuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(name: String, email: String, password: String, phone: String?): Result<Unit>
    fun logout()
    fun isLoggedIn(): Boolean
}
