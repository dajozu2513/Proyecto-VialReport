package com.vialreport.app.presentation.profile

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val saved: Boolean = false,
    val error: String? = null
)
