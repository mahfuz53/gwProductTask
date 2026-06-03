package com.gwproductsusa.gwtasks.presentation.updateaccount

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gwproductsusa.gwtasks.core.di.IoDispatcher
import com.gwproductsusa.gwtasks.core.util.Result
import com.gwproductsusa.gwtasks.domain.usecase.UpdateUserNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
class UpdateAccountViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val updateUserNameUseCase: UpdateUserNameUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val userId: Int = savedStateHandle.get<Int>(ARG_USER_ID) ?: 0

    private val _uiState = MutableStateFlow(
        UpdateAccountUiState(
            userId = userId,
            name = savedStateHandle.get<String>(ARG_USER_NAME).orEmpty()
        )
    )
    val uiState: StateFlow<UpdateAccountUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UpdateAccountUiEvent>()
    val uiEvents: SharedFlow<UpdateAccountUiEvent> = _uiEvents.asSharedFlow()

    fun onAction(action: UpdateAccountUiAction) {
        when (action) {
            is UpdateAccountUiAction.NameChanged -> _uiState.update {
                it.copy(name = action.value, nameError = null, errorMessage = null)
            }
            UpdateAccountUiAction.Submit -> submitUpdate()
            UpdateAccountUiAction.BackClicked -> viewModelScope.launch {
                _uiEvents.emit(UpdateAccountUiEvent.NavigateBack)
            }
            UpdateAccountUiAction.ErrorDismissed -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun submitUpdate() {
        val state = _uiState.value
        val nameError = if (state.name.isBlank()) "Name is required" else null

        if (nameError != null) {
            _uiState.update { it.copy(nameError = nameError) }
            return
        }

        if (state.userId <= 0) {
            _uiState.update { it.copy(errorMessage = "Invalid user session.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            when (
                val result = withContext(ioDispatcher) {
                    updateUserNameUseCase(state.userId, state.name.trim())
                }
            ) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "Account updated successfully!"
                        )
                    }
                    _uiEvents.emit(UpdateAccountUiEvent.NavigateBackAndRefresh)
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
        const val ARG_USER_ID = "userId"
        const val ARG_USER_NAME = "userName"
    }
}
