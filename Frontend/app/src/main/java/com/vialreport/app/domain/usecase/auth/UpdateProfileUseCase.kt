package com.vialreport.app.domain.usecase.auth

import com.vialreport.app.data.local.TokenStore
import com.vialreport.app.data.remote.api.AuthApi
import com.vialreport.app.data.remote.dto.UpdateProfileRequestDto
import com.vialreport.app.data.remote.dto.UserDto
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(name: String, phone: String?): UserDto {
        val response = api.updateProfile(UpdateProfileRequestDto(name, phone))
        val user = response.data ?: error(response.message)
        // Actualizar nombre en TokenStore para que el saludo refleje el cambio
        tokenStore.userName = user.name
        return user
    }
}
