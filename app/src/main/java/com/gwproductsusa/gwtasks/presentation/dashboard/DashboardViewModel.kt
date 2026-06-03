package com.gwproductsusa.gwtasks.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gwproductsusa.gwtasks.core.di.IoDispatcher
import com.gwproductsusa.gwtasks.core.util.Result
import com.gwproductsusa.gwtasks.domain.model.Task
import com.gwproductsusa.gwtasks.domain.model.TaskStage
import com.gwproductsusa.gwtasks.domain.usecase.CheckSessionUseCase
import com.gwproductsusa.gwtasks.domain.usecase.LoadDashboardUseCase
import com.gwproductsusa.gwtasks.domain.usecase.LogoutUseCase
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
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val loadDashboardUseCase: LoadDashboardUseCase,
    private val checkSessionUseCase: CheckSessionUseCase,
    private val logoutUseCase: LogoutUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<DashboardUiEvent>()
    val uiEvents: SharedFlow<DashboardUiEvent> = _uiEvents.asSharedFlow()

    init {
        loadDashboard()
    }

    fun onAction(action: DashboardUiAction) {
        when (action) {
            DashboardUiAction.Refresh -> loadDashboard(isRefresh = true)
            DashboardUiAction.RefreshAfterTaskCreated -> {
                _uiState.update { it.copy(successMessage = "Task created successfully!") }
                loadDashboard(isRefresh = true)
            }
            DashboardUiAction.Logout -> logout()
            DashboardUiAction.ErrorDismissed -> _uiState.update { it.copy(errorMessage = null) }
            DashboardUiAction.SuccessDismissed -> _uiState.update { it.copy(successMessage = null) }
        }
    }

    private fun loadDashboard(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!checkSessionUseCase()) {
                _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
                _uiEvents.emit(DashboardUiEvent.NavigateToLogin)
                return@launch
            }

            val userId = checkSessionUseCase.getUserId()
            _uiState.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            val result = withContext(ioDispatcher) { loadDashboardUseCase(userId) }
            when (result) {
                is Result.Success -> {
                    val (user, tasks) = result.data
                    val successMessage = _uiState.value.successMessage
                    _uiState.value = DashboardUiState(
                        isLoading = false,
                        isRefreshing = false,
                        userName = user.name,
                        userEmail = user.email.ifBlank { user.login },
                        userInitials = buildInitials(user.name),
                        tasks = tasks.map { it.toTaskItemUiState() },
                        errorMessage = null,
                        successMessage = successMessage
                    )
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.error.message
                        )
                    }
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            withContext(ioDispatcher) { logoutUseCase() }
            _uiEvents.emit(DashboardUiEvent.NavigateToLogin)
        }
    }

    private fun Task.toTaskItemUiState() = TaskItemUiState(
        id = id,
        name = name,
        stageName = stageName,
        stage = TaskStage.fromStageName(stageName),
        description = description,
        dueDate = dueDate
    )

    private fun buildInitials(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}".uppercase()
            parts.isNotEmpty() -> parts.first().take(2).uppercase()
            else -> "?"
        }
    }

    companion object {
        const val REFRESH_DASHBOARD_KEY = "refresh_dashboard"
    }
}
