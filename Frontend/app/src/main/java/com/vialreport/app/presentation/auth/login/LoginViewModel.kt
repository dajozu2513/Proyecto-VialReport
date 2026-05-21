package com.vialreport.app.presentation.auth.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vialreport.app.domain.usecase.auth.IsLoggedInUseCase
import com.vialreport.app.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    var email by mutableStateOf("")
    var password by mutableStateOf("")

    fun isAlreadyLoggedIn(): Boolean = isLoggedInUseCase()

    fun login() {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = loginUseCase(email.trim(), password)
            _uiState.value = if (result.isSuccess) {
                LoginUiState.Success
            } else {
                val msg = result.exceptionOrNull()?.message ?: ""
                LoginUiState.Error(
                    when {
                        msg.contains("401") -> "Correo o contraseña incorrectos"
                        msg.contains("404") -> "Usuario no encontrado"
                        msg.isBlank()       -> "Error al iniciar sesión"
                        else                -> msg
                    }
                )
            }
        }
    }

    fun clearError() {
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Idle
        }
    }
}
