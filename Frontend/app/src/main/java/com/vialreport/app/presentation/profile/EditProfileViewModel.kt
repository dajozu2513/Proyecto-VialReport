package com.vialreport.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vialreport.app.data.remote.api.AuthApi
import com.vialreport.app.domain.usecase.auth.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            runCatching { authApi.getMe() }
                .onSuccess { response ->
                    val user = response.data
                    if (user != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                name  = user.name,
                                email = user.email,
                                phone = user.phone ?: ""
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "No se pudo cargar el perfil") }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar perfil") }
                }
        }
    }

    fun onNameChange(v: String)  = _uiState.update { it.copy(name = v, error = null) }
    fun onPhoneChange(v: String) = _uiState.update { it.copy(phone = v, error = null) }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre no puede estar vacío") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            runCatching {
                updateProfileUseCase(
                    name  = state.name.trim(),
                    phone = state.phone.trim().ifBlank { null }
                )
            }
                .onSuccess { _uiState.update { it.copy(isSaving = false, saved = true) } }
                .onFailure { e -> _uiState.update { it.copy(isSaving = false, error = e.message ?: "Error al guardar") } }
        }
    }
}
