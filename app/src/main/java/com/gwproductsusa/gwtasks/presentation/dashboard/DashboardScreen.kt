package com.gwproductsusa.gwtasks.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gwproductsusa.gwtasks.presentation.dashboard.components.DashboardLoading
import com.gwproductsusa.gwtasks.presentation.dashboard.components.DashboardTopBar
import com.gwproductsusa.gwtasks.presentation.dashboard.components.ProfileCard
import com.gwproductsusa.gwtasks.presentation.dashboard.components.TaskCard
import com.gwproductsusa.gwtasks.ui.theme.OdooPurple
import com.gwproductsusa.gwtasks.ui.theme.SurfaceGray
import androidx.compose.foundation.background
import androidx.compose.runtime.remember

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onAction: (DashboardUiAction) -> Unit,
    onCreateTaskClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val onRefresh = rememberUpdatedState { onAction(DashboardUiAction.Refresh) }
    val onLogout = rememberUpdatedState { onAction(DashboardUiAction.Logout) }

    // Derive directly from uiState — remember+derivedStateOf does not track plain parameters
    // and will keep the initial loading value forever across recompositions.
    val showLoading = uiState.isLoading && uiState.userName.isEmpty()

    val taskCountText = run {
        val count = uiState.tasks.size
        if (count == 1) "1 Task" else "$count Tasks"
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onAction(DashboardUiAction.ErrorDismissed)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onAction(DashboardUiAction.SuccessDismissed)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SurfaceGray,
        topBar = {
            DashboardTopBar(
                onRefresh = { onRefresh.value() },
                onLogout = { onLogout.value() },
                isRefreshing = uiState.isRefreshing
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!showLoading) {
                FloatingActionButton(
                    onClick = onCreateTaskClick,
                    containerColor = OdooPurple,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create task")
                }
            }
        }
    ) { padding ->
        if (showLoading) {
            DashboardLoading(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            DashboardContent(
                uiState = uiState,
                taskCountText = taskCountText,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    taskCountText: String,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.background(SurfaceGray),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "profile") {
            ProfileCard(
                userName = uiState.userName,
                userEmail = uiState.userEmail,
                userInitials = uiState.userInitials
            )
        }

        item(key = "tasks_header") {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your Tasks",
                fontWeight = FontWeight.Bold
            )
            Text(text = taskCountText)
        }

        if (uiState.tasks.isEmpty()) {
            item(key = "empty") {
                Text(text = "No tasks found.")
            }
        } else {
            items(
                items = uiState.tasks,
                key = { it.id }
            ) { task ->
                TaskCard(task = task)
            }
        }
    }
}
