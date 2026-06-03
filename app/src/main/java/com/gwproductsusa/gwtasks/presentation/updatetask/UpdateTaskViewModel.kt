package com.gwproductsusa.gwtasks.presentation.updatetask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gwproductsusa.gwtasks.core.di.IoDispatcher
import com.gwproductsusa.gwtasks.core.util.Result
import com.gwproductsusa.gwtasks.domain.usecase.LoadStagesUseCase
import com.gwproductsusa.gwtasks.domain.usecase.UpdateTaskStageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import android.net.Uri
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class UpdateTaskViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadStagesUseCase: LoadStagesUseCase,
    private val updateTaskStageUseCase: UpdateTaskStageUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val taskId: Int = savedStateHandle.get<Int>(ARG_TASK_ID) ?: 0
    private val initialStageId: Int = savedStateHandle.get<Int>(ARG_STAGE_ID) ?: 0

    private val _uiState = MutableStateFlow(
        UpdateTaskUiState(
            taskId = taskId,
            taskTitle = savedStateHandle.get<String>(ARG_TASK_TITLE).orEmpty(),
            deadline = savedStateHandle.get<String>(ARG_DEADLINE).orEmpty(),
            selectedStageId = initialStageId
        )
    )
    val uiState: StateFlow<UpdateTaskUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UpdateTaskUiEvent>()
    val uiEvents: SharedFlow<UpdateTaskUiEvent> = _uiEvents.asSharedFlow()

    init {
        loadStages()
    }

    fun onAction(action: UpdateTaskUiAction) {
        when (action) {
            is UpdateTaskUiAction.StageSelected -> _uiState.update {
                it.copy(selectedStageId = action.stageId, errorMessage = null)
            }
            UpdateTaskUiAction.Submit -> submitUpdate()
            UpdateTaskUiAction.BackClicked -> viewModelScope.launch {
                _uiEvents.emit(UpdateTaskUiEvent.NavigateBack)
            }
            UpdateTaskUiAction.ErrorDismissed -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadStages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStages = true, errorMessage = null) }
            when (val result = withContext(ioDispatcher) { loadStagesUseCase() }) {
                is Result.Success -> {
                    val stages = result.data.map { StageOptionUiState(id = it.id, name = it.name) }
                    val resolvedStageId = stages
                        .firstOrNull { it.id == initialStageId }
                        ?.id
                        ?: initialStageId.takeIf { it > 0 }
                        ?: stages.firstOrNull()?.id
                        ?: 0
                    _uiState.update {
                        it.copy(
                            isLoadingStages = false,
                            stages = stages,
                            selectedStageId = resolvedStageId
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoadingStages = false, errorMessage = result.error.message)
                    }
                }
            }
        }
    }

    private fun submitUpdate() {
        val state = _uiState.value
        if (state.taskId <= 0) {
            _uiState.update { it.copy(errorMessage = "Invalid task.") }
            return
        }
        if (state.selectedStageId <= 0) {
            _uiState.update { it.copy(errorMessage = "Please select a stage.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            when (
                val result = withContext(ioDispatcher) {
                    updateTaskStageUseCase(state.taskId, state.selectedStageId)
                }
            ) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "Task updated successfully!"
                        )
                    }
                    _uiEvents.emit(UpdateTaskUiEvent.NavigateBackAndRefresh)
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = result.error.message)
                    }
                }
            }
        }
    }

    companion object {
        const val ARG_TASK_ID = "taskId"
        const val ARG_TASK_TITLE = "taskTitle"
        const val ARG_DEADLINE = "deadline"
        const val ARG_STAGE_ID = "stageId"
    }
}
