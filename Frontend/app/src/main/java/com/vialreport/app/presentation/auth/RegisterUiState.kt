package com.vialreport.app.presentation.auth

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val canRegister: Boolean
        get() = name.isNotBlank() && email.isNotBlank() && password.length >= 6 && !isLoading
}
