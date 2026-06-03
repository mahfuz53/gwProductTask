package com.gwproductsusa.gwtasks.presentation.updateaccount

import androidx.compose.runtime.Immutable

@Immutable
data class UpdateAccountUiState(
    val userId: Int = 0,
    val name: String = "",
    val isSubmitting: Boolean = false,
    val nameError: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

sealed interface UpdateAccountUiEvent {
    data object NavigateBack : UpdateAccountUiEvent
    data object NavigateBackAndRefresh : UpdateAccountUiEvent
}

sealed interface UpdateAccountUiAction {
    data class NameChanged(val value: String) : UpdateAccountUiAction
    data object Submit : UpdateAccountUiAction
    data object BackClicked : UpdateAccountUiAction
    data object ErrorDismissed : UpdateAccountUiAction
}
