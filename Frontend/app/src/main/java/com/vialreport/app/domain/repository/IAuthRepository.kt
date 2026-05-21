package com.vialreport.app.domain.repository

import com.vialreport.app.domain.model.User

interface IAuthRepository {
    suspend fun login(email: String, password: String): User
    suspend fun register(name: String, email: String, password: String, phone: String?): User
}
