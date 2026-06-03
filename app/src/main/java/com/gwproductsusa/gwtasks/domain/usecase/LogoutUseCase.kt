package com.gwproductsusa.gwtasks.domain.usecase

import com.gwproductsusa.gwtasks.domain.repository.OdooRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: OdooRepository
) {
    suspend operator fun invoke() = repository.logout()
}
