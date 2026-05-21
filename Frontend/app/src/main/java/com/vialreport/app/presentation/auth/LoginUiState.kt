package com.vialreport.app.presentation.auth

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val canLogin: Boolean get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}
