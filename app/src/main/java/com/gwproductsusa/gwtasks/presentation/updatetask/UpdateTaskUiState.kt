package com.gwproductsusa.gwtasks.presentation.updatetask

import androidx.compose.runtime.Immutable

@Immutable
data class StageOptionUiState(
    val id: Int,
    val name: String
)

@Immutable
data class UpdateTaskUiState(
    val taskId: Int = 0,
    val taskTitle: String = "",
    val deadline: String = "",
    val isLoadingStages: Boolean = false,
    val isSubmitting: Boolean = false,
    val stages: List<StageOptionUiState> = emptyList(),
    val selectedStageId: Int = 0,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val selectedStageName: String
        get() = stages.find { it.id == selectedStageId }?.name.orEmpty()

    val isBusy: Boolean
        get() = isLoadingStages || isSubmitting
}

sealed interface UpdateTaskUiEvent {
    data object NavigateBack : UpdateTaskUiEvent
    data object NavigateBackAndRefresh : UpdateTaskUiEvent
}

sealed interface UpdateTaskUiAction {
    data class StageSelected(val stageId: Int) : UpdateTaskUiAction
    data object Submit : UpdateTaskUiAction
    data object BackClicked : UpdateTaskUiAction
    data object ErrorDismissed : UpdateTaskUiAction
}
