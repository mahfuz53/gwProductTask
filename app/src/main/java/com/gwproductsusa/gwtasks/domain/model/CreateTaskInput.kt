package com.gwproductsusa.gwtasks.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class CreateTaskInput(
    val name: String,
    val description: String,
    val dateDeadline: String
)
