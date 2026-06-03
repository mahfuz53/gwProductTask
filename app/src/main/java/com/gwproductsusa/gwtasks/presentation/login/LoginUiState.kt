package com.gwproductsusa.gwtasks.presentation.login

import androidx.compose.runtime.Immutable

@Immutable
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)

sealed interface LoginUiEvent {
    data object NavigateToDashboard : LoginUiEvent
}

sealed interface LoginUiAction {
    data class EmailChanged(val email: String) : LoginUiAction
    data class PasswordChanged(val password: String) : LoginUiAction
    data object LoginClicked : LoginUiAction
    data object ErrorDismissed : LoginUiAction
}
