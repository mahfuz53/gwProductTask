package com.gwproductsusa.gwtasks.presentation.createtask

import androidx.compose.runtime.Immutable

@Immutable
data class CreateTaskUiState(
    val isSubmitting: Boolean = false,
    val taskName: String = "",
    val description: String = "",
    val deadlineDate: String = "",
    val taskNameError: String? = null,
    val deadlineError: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

sealed interface CreateTaskUiEvent {
    data object NavigateBack : CreateTaskUiEvent
    data object NavigateBackAndRefreshDashboard : CreateTaskUiEvent
}

sealed interface CreateTaskUiAction {
    data class TaskNameChanged(val value: String) : CreateTaskUiAction
    data class DescriptionChanged(val value: String) : CreateTaskUiAction
    data class DeadlineChanged(val value: String) : CreateTaskUiAction
    data object Submit : CreateTaskUiAction
    data object BackClicked : CreateTaskUiAction
    data object ErrorDismissed : CreateTaskUiAction
}
