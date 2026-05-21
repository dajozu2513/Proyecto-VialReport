package com.vialreport.app.data.repository

import com.vialreport.app.data.local.TokenStore
import com.vialreport.app.data.remote.api.AuthApi
import com.vialreport.app.data.remote.dto.LoginRequestDto
import com.vialreport.app.data.remote.dto.RegisterRequestDto
import com.vialreport.app.domain.model.User
import com.vialreport.app.domain.repository.IAuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenStore
) : IAuthRepository {

    override suspend fun login(email: String, password: String): User {
        val response = api.login(LoginRequestDto(email, password))
        val data = response.data ?: error(response.message)
        tokenStore.token    = data.token
        tokenStore.role     = data.user.role
        tokenStore.userName = data.user.name
        return User(data.user.id, data.user.name, data.user.email, data.user.role)
    }

    override suspend fun register(name: String, email: String, password: String, phone: String?): User {
        val response = api.register(RegisterRequestDto(name, email, password, phone))
        val data = response.data ?: error(response.message)
        tokenStore.token    = data.token
        tokenStore.role     = data.user.role
        tokenStore.userName = data.user.name
        return User(data.user.id, data.user.name, data.user.email, data.user.role)
    }

    override fun isLoggedIn(): Boolean = tokenStore.token != null

    override fun logout() = tokenStore.clear()
}
