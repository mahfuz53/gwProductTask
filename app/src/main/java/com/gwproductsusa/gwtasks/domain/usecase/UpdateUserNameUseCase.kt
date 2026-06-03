package com.gwproductsusa.gwtasks.domain.usecase

import com.gwproductsusa.gwtasks.core.util.Result
import com.gwproductsusa.gwtasks.domain.repository.OdooRepository
import javax.inject.Inject

class UpdateUserNameUseCase @Inject constructor(
    private val repository: OdooRepository
) {
    suspend operator fun invoke(userId: Int, name: String): Result<Boolean> =
        repository.updateUserName(userId, name)
}
