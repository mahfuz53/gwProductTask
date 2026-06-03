package com.gwproductsusa.gwtasks.data.mapper

import com.gwproductsusa.gwtasks.data.remote.dto.UserDto
import com.gwproductsusa.gwtasks.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email.orEmpty(),
    login = login.orEmpty()
)

fun List<UserDto>.toDomainUsers(currentUserId: Int): User? =
    firstOrNull { it.id == currentUserId }?.toDomain()
        ?: firstOrNull()?.toDomain()
