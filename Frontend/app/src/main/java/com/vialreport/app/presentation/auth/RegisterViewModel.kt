package com.vialreport.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vialreport.app.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v, error = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, error = null) }
    fun onPhoneChange(v: String) = _uiState.update { it.copy(phone = v) }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.canRegister) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                registerUseCase(
                    name     = state.name.trim(),
                    email    = state.email.trim(),
                    password = state.password,
                    phone    = state.phone.takeIf { it.isNotBlank() }
                )
            }
                .onSuccess { onSuccess() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message ?: "Error al registrarse") } }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
