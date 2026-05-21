package com.vialreport.app.domain.usecase.auth

import com.vialreport.app.domain.repository.IAuthRepository
import javax.inject.Inject

class IsLoggedInUseCase @Inject constructor(private val repository: IAuthRepository) {
    operator fun invoke(): Boolean = repository.isLoggedIn()
}
