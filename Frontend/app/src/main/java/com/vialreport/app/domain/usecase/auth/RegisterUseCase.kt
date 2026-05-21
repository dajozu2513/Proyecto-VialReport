package com.vialreport.app.domain.usecase.auth

import com.vialreport.app.domain.model.User
import com.vialreport.app.domain.repository.IAuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        phone: String?
    ): User = repository.register(name, email, password, phone)
}
