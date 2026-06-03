package com.gwproductsusa.gwtasks.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gwproductsusa.gwtasks.presentation.createtask.CreateTaskScreen
import com.gwproductsusa.gwtasks.presentation.createtask.CreateTaskUiEvent
import com.gwproductsusa.gwtasks.presentation.createtask.CreateTaskViewModel
import com.gwproductsusa.gwtasks.presentation.dashboard.DashboardScreen
import com.gwproductsusa.gwtasks.presentation.dashboard.DashboardUiAction
import com.gwproductsusa.gwtasks.presentation.dashboard.DashboardUiEvent
import com.gwproductsusa.gwtasks.presentation.dashboard.DashboardViewModel
import com.gwproductsusa.gwtasks.presentation.login.LoginScreen
import com.gwproductsusa.gwtasks.presentation.login.LoginUiEvent
import com.gwproductsusa.gwtasks.presentation.login.LoginViewModel
import com.gwproductsusa.gwtasks.presentation.updatetask.UpdateTaskScreen
import com.gwproductsusa.gwtasks.presentation.updatetask.UpdateTaskUiEvent
import com.gwproductsusa.gwtasks.presentation.updatetask.UpdateTaskViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GwTasksNavGraph(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            val viewModel: LoginViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.uiEvents.collectLatest { event ->
                    when (event) {
                        LoginUiEvent.NavigateToDashboard -> {
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    }
                }
            }

            LoginScreen(
                uiState = uiState,
                onAction = viewModel::onAction
            )
        }

        composable(Routes.DASHBOARD) {
            val viewModel: DashboardViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.uiEvents.collectLatest { event ->
                    when (event) {
                        DashboardUiEvent.NavigateToLogin -> {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.DASHBOARD) { inclusive = true }
                            }
                        }
                    }
                }
            }

            LaunchedEffect(navController.currentBackStackEntry) {
                val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
                handle.getStateFlow(DashboardViewModel.REFRESH_DASHBOARD_KEY, false)
                    .collect { shouldRefresh ->
                        if (shouldRefresh) {
                            viewModel.onAction(DashboardUiAction.RefreshAfterTaskCreated)
                            handle[DashboardViewModel.REFRESH_DASHBOARD_KEY] = false
                        }
                    }
            }

            LaunchedEffect(navController.currentBackStackEntry) {
                val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
                handle.getStateFlow(DashboardViewModel.REFRESH_AFTER_TASK_UPDATED, false)
                    .collect { shouldRefresh ->
                        if (shouldRefresh) {
                            viewModel.onAction(DashboardUiAction.RefreshAfterTaskUpdated)
                            handle[DashboardViewModel.REFRESH_AFTER_TASK_UPDATED] = false
                        }
                    }
            }

            DashboardScreen(
                uiState = uiState,
                onAction = viewModel::onAction,
                onCreateTaskClick = { navController.navigate(Routes.CREATE_TASK) },
                onTaskClick = { task ->
                    navController.navigate(
                        Routes.updateTask(
                            taskId = task.id,
                            taskTitle = task.name,
                            deadline = task.dueDate,
                            stageId = task.stageId
                        )
                    )
                }
            )
        }

        composable(Routes.CREATE_TASK) {
            val viewModel: CreateTaskViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.uiEvents.collectLatest { event ->
                    when (event) {
                        CreateTaskUiEvent.NavigateBack -> navController.popBackStack()
                        CreateTaskUiEvent.NavigateBackAndRefreshDashboard -> {
                            navController.getBackStackEntry(Routes.DASHBOARD)
                                .savedStateHandle[DashboardViewModel.REFRESH_DASHBOARD_KEY] = true
                            navController.popBackStack()
                        }
                    }
                }
            }

            CreateTaskScreen(
                uiState = uiState,
                onAction = viewModel::onAction
            )
        }

        composable(
            route = "${Routes.UPDATE_TASK}/{${UpdateTaskViewModel.ARG_TASK_ID}}/{${UpdateTaskViewModel.ARG_TASK_TITLE}}/{${UpdateTaskViewModel.ARG_DEADLINE}}/{${UpdateTaskViewModel.ARG_STAGE_ID}}",
            arguments = listOf(
                navArgument(UpdateTaskViewModel.ARG_TASK_ID) { type = NavType.IntType },
                navArgument(UpdateTaskViewModel.ARG_TASK_TITLE) { type = NavType.StringType },
                navArgument(UpdateTaskViewModel.ARG_DEADLINE) { type = NavType.StringType },
                navArgument(UpdateTaskViewModel.ARG_STAGE_ID) { type = NavType.IntType }
            )
        ) {
            val viewModel: UpdateTaskViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.uiEvents.collectLatest { event ->
                    when (event) {
                        UpdateTaskUiEvent.NavigateBack -> navController.popBackStack()
                        UpdateTaskUiEvent.NavigateBackAndRefresh -> {
                            navController.getBackStackEntry(Routes.DASHBOARD)
                                .savedStateHandle[DashboardViewModel.REFRESH_AFTER_TASK_UPDATED] = true
                            navController.popBackStack()
                        }
                    }
                }
            }

            UpdateTaskScreen(
                uiState = uiState,
                onAction = viewModel::onAction
            )
        }
    }
}
