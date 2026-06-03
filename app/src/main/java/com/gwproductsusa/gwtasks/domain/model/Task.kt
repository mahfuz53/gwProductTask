package com.gwproductsusa.gwtasks.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Task(
    val id: Int,
    val name: String,
    val stageName: String,
    val description: String,
    val dueDate: String
)

enum class TaskStage(val displayName: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    UNKNOWN("Unknown");

    companion object {
        fun fromStageName(name: String): TaskStage = when {
            name.contains("pending", ignoreCase = true) -> PENDING
            name.contains("progress", ignoreCase = true) -> IN_PROGRESS
            name.contains("done", ignoreCase = true) ||
                name.contains("complete", ignoreCase = true) -> COMPLETED
            else -> UNKNOWN
        }
    }
}
