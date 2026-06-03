package com.gwproductsusa.gwtasks.presentation.createtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gwproductsusa.gwtasks.core.di.IoDispatcher
import com.gwproductsusa.gwtasks.core.util.Result
import com.gwproductsusa.gwtasks.domain.model.CreateTaskInput
import com.gwproductsusa.gwtasks.domain.usecase.CreateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val createTaskUseCase: CreateTaskUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<CreateTaskUiEvent>()
    val uiEvents: SharedFlow<CreateTaskUiEvent> = _uiEvents.asSharedFlow()

    fun onAction(action: CreateTaskUiAction) {
        when (action) {
            is CreateTaskUiAction.TaskNameChanged -> _uiState.update {
                it.copy(taskName = action.value, taskNameError = null, errorMessage = null)
            }
            is CreateTaskUiAction.DescriptionChanged -> _uiState.update {
                it.copy(description = action.value, errorMessage = null)
            }
            is CreateTaskUiAction.DeadlineChanged -> _uiState.update {
                it.copy(deadlineDate = action.value, deadlineError = null, errorMessage = null)
            }
            CreateTaskUiAction.Submit -> submitTask()
            CreateTaskUiAction.BackClicked -> viewModelScope.launch {
                _uiEvents.emit(CreateTaskUiEvent.NavigateBack)
            }
            CreateTaskUiAction.ErrorDismissed -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun submitTask() {
        val state = _uiState.value
        val taskNameError = if (state.taskName.isBlank()) "Task name is required" else null
        val deadlineError = validateDeadline(state.deadlineDate)

        if (taskNameError != null || deadlineError != null) {
            _uiState.update {
                it.copy(taskNameError = taskNameError, deadlineError = deadlineError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            val input = CreateTaskInput(
                name = state.taskName.trim(),
                description = state.description.trim(),
                dateDeadline = formatDeadlineForApi(state.deadlineDate)
            )
            when (val result = withContext(ioDispatcher) { createTaskUseCase(input) }) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "Task created successfully!"
                        )
                    }
                    _uiEvents.emit(CreateTaskUiEvent.NavigateBackAndRefreshDashboard)
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = result.error.message)
                    }
                }
            }
        }
    }

    private fun validateDeadline(deadline: String): String? = when {
        deadline.isBlank() -> "Deadline date is required"
        formatDeadlineForApi(deadline).isBlank() -> "Enter a valid date (yyyy-MM-dd)"
        else -> null
    }

    private fun formatDeadlineForApi(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""
        val formatters = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MMM d, yyyy"),
            DateTimeFormatter.ofPattern("d MMM yyyy")
        )
        for (formatter in formatters) {
            try {
                return LocalDate.parse(trimmed, formatter).format(DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (_: DateTimeParseException) {
                // try next
            }
        }
        return if (Regex("""\d{4}-\d{2}-\d{2}""").matches(trimmed)) trimmed else ""
    }
}
