package com.gwproductsusa.gwtasks.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val login: String
)
