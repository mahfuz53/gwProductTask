package com.gwproductsusa.gwtasks.presentation.dashboard

import androidx.compose.runtime.Immutable
import com.gwproductsusa.gwtasks.domain.model.TaskStage

@Immutable
data class DashboardUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val userEmail: String = "",
    val userInitials: String = "",
    val tasks: List<TaskItemUiState> = emptyList(),
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)

@Immutable
data class TaskItemUiState(
    val id: Int,
    val name: String,
    val stageName: String,
    val stage: TaskStage,
    val description: String,
    val dueDate: String
)

sealed interface DashboardUiEvent {
    data object NavigateToLogin : DashboardUiEvent
}

sealed interface DashboardUiAction {
    data object Refresh : DashboardUiAction
    data object Logout : DashboardUiAction
    data object ErrorDismissed : DashboardUiAction
}
