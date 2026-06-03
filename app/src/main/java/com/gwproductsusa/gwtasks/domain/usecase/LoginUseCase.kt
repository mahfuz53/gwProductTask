package com.gwproductsusa.gwtasks.domain.usecase

import com.gwproductsusa.gwtasks.core.util.Result
import com.gwproductsusa.gwtasks.domain.repository.OdooRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: OdooRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Int> =
        repository.login(email.trim(), password)
}
