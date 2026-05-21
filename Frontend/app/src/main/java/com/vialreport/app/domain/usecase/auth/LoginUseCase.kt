package com.vialreport.app.domain.usecase.auth

import com.vialreport.app.domain.model.User
import com.vialreport.app.domain.repository.IAuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> =
        runCatching { repository.login(email, password) }
}
